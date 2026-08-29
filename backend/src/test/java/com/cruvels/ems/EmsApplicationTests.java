package com.cruvels.ems;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Sanity test: makes sure the whole Spring context (all beans, security config,
// database connections) wires up correctly. If anything is misconfigured, this fails.
@SpringBootTest
class EmsApplicationTests {
    @Test
    void contextLoads() {
    }
}
