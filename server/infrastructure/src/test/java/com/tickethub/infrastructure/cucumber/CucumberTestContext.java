package com.tickethub.infrastructure.cucumber;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.Main;
import com.tickethub.infrastructure.payment.PaymentGatewayTestConfiguration;

import io.cucumber.spring.CucumberContextConfiguration;

@CucumberContextConfiguration
@SpringBootTest(classes = {Main.class, PaymentGatewayTestConfiguration.class})
@AutoConfigureMockMvc
public class CucumberTestContext extends ContainerSupport {
}
