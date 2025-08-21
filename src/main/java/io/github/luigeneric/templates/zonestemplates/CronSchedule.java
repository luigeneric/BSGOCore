package io.github.luigeneric.templates.zonestemplates;

public record CronSchedule(
        String cronExpression,
        String durationMinutes
) {}
