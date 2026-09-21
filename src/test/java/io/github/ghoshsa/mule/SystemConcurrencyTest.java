package io.github.ghoshsa.mule;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.ghoshsa.mule.inventory.model.InventoryItem;
import io.github.ghoshsa.mule.inventory.repository.InventoryRepository;
import io.github.ghoshsa.mule.order.model.Order;
import io.github.ghoshsa.mule.order.model.OrderItem;
import io.github.ghoshsa.mule.order.model.OrderStatus;
import io.github.ghoshsa.mule.order.repository.OrderRepository;
import io.github.ghoshsa.mule.saga.OrderSagaOrchestrator;

@SpringBootTest
public class SystemConcurrencyTest {
    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OrderSagaOrchestrator orderSagaOrchestrator;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void concurrent_inventoryReservation() throws Exception {
        String productId = "product-concurrent";
        inventoryRepository.save(new InventoryItem(productId, 10));

        int requestCount = 12;
        List<Future<Order>> futureOrders = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(requestCount);
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);

        for (int i = 0; i < 12; i++) {
            final int index = i;

            futureOrders.add(executorService.submit(() -> {
                ready.countDown();
                start.await();

                OrderItem item = new OrderItem();
                item.setProductId(productId);
                item.setQuantity(1);

                return orderSagaOrchestrator.placeOrder("customer-" + index, List.of(item));
            }));
        }

        ready.await();
        start.countDown();

        int confirmed = 0, failed = 0;
        for (Future<Order> future : futureOrders) {
            Order order = future.get();
            if (order.getOrderStatus() == OrderStatus.CONFIRMED) {
                confirmed++;
            } else if (order.getOrderStatus() == OrderStatus.FAILED) {
                failed++;
            }
        }

        executorService.shutdown();

        assertEquals(10, confirmed);
        assertEquals(2, failed);
        assertEquals(12, orderRepository.count());
        assertEquals(0, inventoryRepository.findById(productId).orElseThrow().getAvailableQuantity());
    }
}