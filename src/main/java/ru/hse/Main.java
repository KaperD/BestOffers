package ru.hse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

enum PaymentMethod {
    Card,
    Cash,
    Any;

    public boolean canPayWith(PaymentMethod method) {
        return method == this || this == Any || method == Any;
    }

    public static PaymentMethod parsePaymentMethod(String s) {
        return switch (s.trim()) {
            case "card" -> Card;
            case "cash" -> Cash;
            case "any" -> Any;
            default -> throw new IllegalArgumentException("Wrong Payment Method");
        };
    }
}

class Interval {
    private final int from;
    private final int to;

    public Interval(String s) {
        String[] parts = s.split("-");
        this.from = Integer.parseInt(parts[0].trim());
        this.to = Integer.parseInt(parts[1].trim());
    }

    public boolean includes(int x) {
        return x >= from && x <= to;
    }
}

class Offer {
    private final String serviceName;
    private final Interval carArriveTimeInterval;
    private final Interval priceInterval;
    private final Interval rideTimeInterval;
    private final PaymentMethod paymentMethod;

    public Offer(String serviceName, Interval carArriveTimeInterval, Interval priceInterval, Interval rideTimeInterval, PaymentMethod paymentMethod) {
        this.serviceName = serviceName;
        this.carArriveTimeInterval = carArriveTimeInterval;
        this.priceInterval = priceInterval;
        this.rideTimeInterval = rideTimeInterval;
        this.paymentMethod = paymentMethod;
    }

    public String getServiceName() {
        return serviceName;
    }

    public boolean isGoodFor(int carArriveTime, int price, int rideTime, PaymentMethod paymentMethod) {
        return carArriveTimeInterval.includes(carArriveTime) &&
                priceInterval.includes(price) &&
                rideTimeInterval.includes(rideTime) &&
                this.paymentMethod.canPayWith(paymentMethod);
    }
}

public class Main {
    public static void main(String[] args) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            List<Offer> offers = new ArrayList<>();
            int numberOfWorkers = Runtime.getRuntime().availableProcessors();
            List<List<Offer>> results = new ArrayList<>(numberOfWorkers);
            for (int i = 0; i < numberOfWorkers; i++) {
                results.add(null);
            }
            while (true) {
                String line = bufferedReader.readLine();
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    final int carArriveTime = Integer.parseInt(parts[0].trim());
                    final int price = Integer.parseInt(parts[1].trim());
                    final int rideTime = Integer.parseInt(parts[2].trim());
                    final PaymentMethod paymentMethod = PaymentMethod.parsePaymentMethod(parts[3]);
                    Thread[] threads = new Thread[numberOfWorkers];
                    int partSize = (offers.size() + numberOfWorkers - 1) / numberOfWorkers;
                    for (int i = 0; i < numberOfWorkers; i++) {
                        int ic = i;
                        Thread thread = new Thread(() -> {
                            List<Offer> goodOffers = new ArrayList<>();
                            for (int k = ic * partSize; k < (ic + 1) * partSize && k < offers.size(); k++) {
                                if (offers.get(k).isGoodFor(carArriveTime, price, rideTime, paymentMethod)) {
                                    goodOffers.add(offers.get(k));
                                }
                            }
                            results.set(ic, goodOffers);
                        });
                        thread.start();
                        threads[i] = thread;
                    }
                    for (Thread thread : threads) {
                        thread.join();
                    }
                    List<String> result = new ArrayList<>();
                    for (var res : results) {
                        for (Offer offer : res) {
                            result.add(offer.getServiceName());
                        }
                    }
                    for (String service : result) {
                        System.out.println(service);
                    }
                    return;
                }
                offers.add(new Offer(
                        parts[0].trim(),
                        new Interval(parts[1]),
                        new Interval(parts[2]),
                        new Interval(parts[3]),
                        PaymentMethod.parsePaymentMethod(parts[4])
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}