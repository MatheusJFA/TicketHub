package com.tickethub.infrastructure.cucumber;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.Main;

import io.cucumber.spring.CucumberContextConfiguration;

@CucumberContextConfiguration
@SpringBootTest(classes = Main.class)
@AutoConfigureMockMvc
public class CucumberTestContext extends ContainerSupport {
}
