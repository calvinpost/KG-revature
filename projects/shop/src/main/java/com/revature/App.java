package com.revature;

import com.revature.dao.ItemPostgres;
import com.revature.dao.OfferPostgres;
import com.revature.model.Item;
import com.revature.model.Offer;
import com.revature.model.User;
import com.revature.service.AuthService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class App {
    private static final Logger logger = LogManager.getLogger(App.class);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            logger.info("""
                    Welcome to Random Shop!
                    1) Login
                    2) Register
                    """);
            String input = scanner.nextLine();

            if (input.equals("1")) {
                logger.info("------LOGIN------");
                logger.info("Username: ");
                String username = scanner.nextLine();
                logger.info("Password: ");
                String password = scanner.nextLine();
                logger.info("");
                User user = null;
                try {
                    user = new AuthService().login(username, password);
                } catch (Exception e) {
                    logger.info("Wrong credentials. Please try again");
                    logger.info("");
                    continue;
                }

                if (user.getRole().equals(User.Role.CUSTOMER)) {
                    printCustomerMenu(scanner, user);
                } else if (user.getRole().equals(User.Role.EMPLOYEE)) {
                    printEmployeeMenu(scanner, user);
                } else if (user.getRole().equals(User.Role.MANAGER)) {
                    logger.info(String.format("""
                            -------------MENU---------------
                            Manager: %s        0) logout
                                            
                            1) My items
                            2) My Pending offers
                            3) Available items
                            """, user.getUsername()));
                    logger.info("Manger view not yet implemented\n");
                }


            } else if (input.equals("2")) {
                logger.info("-------REGISTER------");
                logger.info("Username: ");
                String username = scanner.nextLine();
                logger.info("Password: ");
                String password = scanner.nextLine();
                logger.info("");
                try {
                    new AuthService().register(username, password);
                } catch (Exception e) {
                    logger.info(e.getMessage());
                    logger.info("");
                }
            } else {
                logger.info("\nPlease choose 1 or 2\n");
            }
        }


    }

    public static void printCustomerMenu(Scanner scanner, User user) {
        label:
        while (true) {
            logger.info(String.format("""
                    -------------MENU---------------
                    Customer: %s        0) logout
                                    
                    1) My items
                    2) My Pending offers
                    3) Available items
                    """, user.getUsername()));
            ;
            String menuInput = scanner.nextLine();

            switch (menuInput) {
                case "1": {
                    List<Offer> offers = new OfferPostgres().getAll();
                    List<Offer> filtered_offers = offers.stream().filter((offer) -> {
                        return offer.getUser().equals(user) && offer.getItem().getStock().equals(Item.Stock.OWNED);
                    }).toList();

                    logger.info("----------------------MY ITEMS-------------------------");
                    for (Offer offer : filtered_offers) {
                        logger.info("Item: " + offer.getItem().getItemName() + " | Purchase date: " + offer.getDate());
                    }
                    logger.info("");

                    break;
                }
                case "2": {
                    List<Offer> offers = new OfferPostgres().getAll();
                    List<Offer> filtered_offers = offers.stream().filter((offer) -> {
                        return offer.getUser().equals(user) && offer.getItem().getStock().equals(Item.Stock.AVAILABLE);
                    }).toList();

                    logger.info("----------------------MY PENDING OFFERS-------------------------");
                    for (Offer offer : filtered_offers) {
                        logger.info("Item: " + offer.getItem().getItemName() + " | Offer Amount: " + offer.getAmount() + " | Offer Date: " + offer.getDate());
                    }
                    logger.info("");
                    break;
                }
                case "3":
                    List<Item> items = new ItemPostgres().getAll();
                    logger.info("--------INVENTORY--------");
                    for (Item item : items) {
                        if (item.getStock().equals(Item.Stock.AVAILABLE)) {
                            logger.info(String.format("Item: %s | Stock: %s", item.getItemName(), item.getStock()));
                        }
                    }

                    logger.info("""
                            ------------------------
                            1) Menu
                            2) Make an offer
                            """);

                    String inventoryInput = scanner.nextLine();

                    if (inventoryInput.equals("2")) {
                        logger.info("---------MAKE AN OFFER----------");
                        logger.info("Item: ");
                        String itemInput = scanner.nextLine();
                        logger.info("Offer amount: ");
                        String offerInput = scanner.nextLine();

                        List<Item> filteredItem = items.stream().filter((i) -> i.getItemName().equals(itemInput)).toList();
                        if (filteredItem.isEmpty()) {
                            logger.info("\nNo item with that name. Returning to Menu\n");
                            continue;
                        }
                        Offer offer = new Offer(user, filteredItem.get(0), LocalDate.now(), Double.parseDouble(offerInput));
                        new OfferPostgres().insert(offer);
                    } else {
                        logger.info("Please choose 1 or 2\n");
                    }
                    break;
                case "0":
                    logger.info("Thank you for shopping!\n");
                    break label;
                default:
                    logger.info("Please choose 1, 2, 3 or 4");
                    break;
            }
        }
    }

    public static void printEmployeeMenu(Scanner scanner, User user) {
        label:
        while (true) {
            logger.info(String.format("""
                    -------------MENU---------------
                    Employee: %s        0) logout
                                    
                    1) View all Offers
                    2) View all Items
                    """, user.getUsername()));
            ;
            String menuInput = scanner.nextLine();

            switch (menuInput) {
                case "1": {
                    List<Offer> offers = new OfferPostgres().getAll();
                    logger.info("----------------------ALL OFFERS-------------------------");
                    for (Offer offer : offers) {
                        logger.info("Item: " + offer.getItem().getItemName()
                                + " | Customer: " + offer.getUser().getUsername()
                                + " | Offer Amount: " + offer.getAmount()
                                + " | Offer Date: " + offer.getDate()
                                + " | Item stock: " + offer.getItem().getStock());
                    }
                    logger.info("""
                            ------------------------
                            1) Accept Offer
                            2) Reject Offer
                            3) Menu
                            """);

                    String allOffersInput = scanner.nextLine();
                    switch (allOffersInput) {
                        case "1": {
                            logger.info("---------ACCEPT OFFER----------");
                            logger.info("Customer name: ");
                            String customerNameInput = scanner.nextLine();
                            logger.info("Item name: ");
                            String itemNameInput = scanner.nextLine();

                            List<Offer> filtered_offers = offers.stream().filter((offer) -> {
                                return offer.getUser().getUsername().equals(customerNameInput)
                                        && offer.getItem().getItemName().equals(itemNameInput);
                            }).toList();

                            if (filtered_offers.isEmpty()) {
                                logger.info("\nNo item or user with that name. Returning to Menu\n");
                                continue;
                            }

                            Item item = filtered_offers.get(0).getItem();
                            item.setStock(Item.Stock.OWNED);
                            new ItemPostgres().update(item);

                            //System: remove pending offers for the same item
                            for (Offer offer : offers) {
                                if (!offer.getUser().getUsername().equals(customerNameInput) && offer.getItem().getItemName().equals(itemNameInput)) {
                                    new OfferPostgres().delete(offer.getUser().getId(), offer.getItem().getId());
                                }
                            }

                            break;
                        }
                        case "2": {
                            logger.info("---------REJECT OFFER----------");
                            logger.info("Customer name: ");
                            String customerNameInput = scanner.nextLine();
                            logger.info("Item name: ");
                            String itemNameInput = scanner.nextLine();

                            List<Offer> filtered_offers = offers.stream().filter((offer) -> {
                                return offer.getUser().getUsername().equals(customerNameInput)
                                        && offer.getItem().getItemName().equals(itemNameInput);
                            }).toList();

                            if (filtered_offers.isEmpty()) {
                                logger.info("\nNo item or user with that name. Returning to Menu\n");
                                continue;
                            }
                            if (filtered_offers.get(0).getItem().getStock().equals(Item.Stock.AVAILABLE)) {

                                new OfferPostgres().delete(filtered_offers.get(0).getUser().getId(), filtered_offers.get(0).getItem().getId());
                            } else {
                                logger.info("\nCan't reject accepted offer");
                            }
                            break;
                        }
                        case "3":
                            continue;
                        default:
                            logger.info("Please choose 1 or 2 or 3\n");
                            break;
                    }
                    logger.info("");
                    break;
                }
                case "2": {
                    logger.info("All items view not yet implemented");
                    break;
                }
                case "0":
                    logger.info("Thank you for being our employee!\n");
                    break label;
                default:
                    logger.info("Please choose 1, 2, 3 or 4");
                    break;
            }
        }
    }
}
