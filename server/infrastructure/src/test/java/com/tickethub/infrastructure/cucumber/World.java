package com.tickethub.infrastructure.cucumber;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;

import io.cucumber.spring.ScenarioScope;

@Component
@ScenarioScope
public class World {

    private final Map<String, String> partners = new HashMap<>();
    private String currentPartner;
    private String token;
    private String showId;
    private String sectionId;
    private String spotId;
    private String spotLocation;
    private MvcResult lastResult;

    public void reset() {
        partners.clear();
        currentPartner = null;
        token = null;
        showId = null;
        sectionId = null;
        spotId = null;
        spotLocation = null;
        lastResult = null;
    }

    public Map<String, String> partners() {
        return partners;
    }

    public String currentPartner() {
        return currentPartner;
    }

    public void currentPartner(final String currentPartner) {
        this.currentPartner = currentPartner;
    }

    public String token() {
        return token;
    }

    public void token(final String token) {
        this.token = token;
    }

    public String showId() {
        return showId;
    }

    public void showId(final String showId) {
        this.showId = showId;
    }

    public String sectionId() {
        return sectionId;
    }

    public void sectionId(final String sectionId) {
        this.sectionId = sectionId;
    }

    public String spotId() {
        return spotId;
    }
    public void spotId(final String spotId) {
        this.spotId = spotId;
    }

    public String spotLocation() {
        return spotLocation;
    }

    public void spotLocation(final String spotLocation) {
        this.spotLocation = spotLocation;
    }

    public MvcResult lastResult() {
        return lastResult;
    }

    public void lastResult(final MvcResult lastResult) {
        this.lastResult = lastResult;
    }
}
