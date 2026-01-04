package com.example.rolldice;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@RestController
public class RolldiceController {
    private static final Logger logger = LoggerFactory.getLogger(RolldiceController.class);
    private static final LongCounter diceRollCounter;

    static {
        diceRollCounter = GlobalOpenTelemetry.getMeter("dice-roller")
            .counterBuilder("dice.rolls")
            .setDescription("The number of times each value was rolled")
            .build();
    }

    @GetMapping("/rolldice")
    public String index(@RequestParam("player") Optional<String> player) {
        int result = this.getRandomNumber(1, 6);
        
        // Increment the counter with the rolled value
        diceRollCounter.add(1, Attributes.of(
            AttributeKey.stringKey("value"), 
            String.valueOf(result)
        ));

        if (player.isPresent() && !player.get().isEmpty()) {
            logger.info("Player {} is rolling the dice, result: {}", player.get(), result);
        } else {
            logger.info("Anonymous player is rolling the dice, result: {}", result);
        }
        return Integer.toString(result) + "\n";
    }

    public int getRandomNumber(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
