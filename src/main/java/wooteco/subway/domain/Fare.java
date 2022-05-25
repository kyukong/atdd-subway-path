package wooteco.subway.domain;

public class Fare {

    private static final double DEFAULT_FARE = 1250;
    private static final double MAXIMUM_DISTANCE_OF_DEFAULT_FARE = 10;
    private static final double MINIMUM_DISTANCE_OF_MAXIMUM_FARE = 50;
    private static final double DISTANCE_UNIT_UNDER_50 = 5;
    private static final double DISTANCE_UNIT_OVER_50 = 8;
    private static final double ADDITIONAL_AMOUNT = 100;

    private Fare() {
    }

    public static double calculate(final double distance, final int extraFare) {
        double fare = DEFAULT_FARE + extraFare;
        if (distance <= MAXIMUM_DISTANCE_OF_DEFAULT_FARE) {
            return fare;
        }
        if (distance <= MINIMUM_DISTANCE_OF_MAXIMUM_FARE) {
            return fare + addExtraFare(distance, DISTANCE_UNIT_UNDER_50, MAXIMUM_DISTANCE_OF_DEFAULT_FARE);
        }
        return fare
                + addExtraFare(MINIMUM_DISTANCE_OF_MAXIMUM_FARE, DISTANCE_UNIT_UNDER_50, MAXIMUM_DISTANCE_OF_DEFAULT_FARE)
                + addExtraFare(distance, DISTANCE_UNIT_OVER_50, MINIMUM_DISTANCE_OF_MAXIMUM_FARE);
    }

    public static double discount(final double fare, final int age) {
        if (age >= 13 && age < 19) {
            return (fare - 350) * 0.8;
        }
        if (age >= 6 && age < 13) {
            return (fare - 350) * 0.5;
        }
        return fare;
    }

    private static double addExtraFare(final double distance, final double distanceUnit, final double limit) {
        return Math.ceil((distance - limit) / distanceUnit) * ADDITIONAL_AMOUNT;
    }
}
