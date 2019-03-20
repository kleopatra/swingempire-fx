/*
 * Created on 19.03.2019
 *
 */
package de.swingempire.fx.property;


import java.util.Optional;
import java.util.logging.Logger;

import static java.util.Optional.*;


/* "I call it my billion-dollar mistake. 
 * It was the invention of the null reference in 1965..."
 *  -- Sir Charles Antony Richard  */

// Get a discount line to print in UI

/**
 * https://github.com/victorrentea/functional-patterns-article/blob/master/src/main/java/victor/training/functional/patterns/C__Optional_Slays_NPE.java
 */
public class DiscountService {
        public String getDiscountLine(Customer customer) {
            Optional<MemberCard> memberCard = customer.getMemberCard();
            // mapper provides Optional wrapped around result
            Optional<Integer> discount = memberCard.flatMap(this::getApplicableDiscountPercentage);
            // mapper provides conversion from source value to result value,
            // source optional wraps result value into optional
            Optional<String> log = discount.map(value -> "discount: " + value);
            LOG.info(log.toString());
            
                return customer.getMemberCard()
                                .flatMap(card -> getApplicableDiscountPercentage(card))
                                .map(d -> {
                                    return "Discount%: " + d;
                                    })
                                .orElse("");
        }
        
        /**
         * MemberCard must not be null.
         * 
         * @param card
         * @return
         */
        private Optional<Integer> getApplicableDiscountPercentage(MemberCard card) { 
                if (card.getFidelityPoints() >= 100) {
                        return of(5);
                }
                if (card.getFidelityPoints() >= 50) {
                        return of(3);
                }
                return empty();
        }
                
        // test: 60, 10, no MemberCard
        public static void main(String[] args) {
                DiscountService discountService = new DiscountService();
                System.out.println(discountService.getDiscountLine(new Customer(new MemberCard(60))));
                System.out.println(discountService.getDiscountLine(new Customer(new MemberCard(10))));
                System.out.println(discountService.getDiscountLine(new Customer()));
        }
// VVVVVVVVV ==== supporting (dummy) code ==== VVVVVVVVV
        static class Customer {
            private MemberCard memberCard;
            public Customer() {
            }
            public Customer(MemberCard profile) {
                this.memberCard = profile;
            }
            public Optional<MemberCard> getMemberCard() {
                return ofNullable(memberCard);
            }
        }
        
        static class MemberCard {
            private final int fidelityPoints;
            
            public MemberCard(int points) {
                this.fidelityPoints = points;
            }
            
            public int getFidelityPoints() {
                return fidelityPoints;
            }
        }
        
        @SuppressWarnings("unused")
        private static final Logger LOG = Logger
                .getLogger(DiscountService.class.getName());
}








