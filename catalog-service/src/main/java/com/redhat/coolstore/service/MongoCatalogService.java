package com.redhat.coolstore.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.redhat.coolstore.model.Product;
import org.bson.Document;


@ApplicationScoped
public class MongoCatalogService implements CatalogService {

    @Inject
    private MongoClient mc;

    @Inject
    Logger log;

    private MongoCollection<Document> productCollection;

	public MongoCatalogService() {
	}

	public List<Product> getProducts() {
        return StreamSupport.stream(productCollection.find().spliterator(), false)
                .map(d -> toProduct(d))
                .collect(Collectors.toList());

    }


    public void add(Product product) {
        productCollection.insertOne(toDocument(product));
    }

    public void addAll(List<Product> products) {
        List<Document> documents = products.stream().map(p -> toDocument(p)).collect(Collectors.toList());
        productCollection.insertMany(documents);
    }

    @PostConstruct
    protected void init() {
        log.info("@PostConstruct is called...");

        String dbName = System.getenv("DB_NAME");
        if(dbName==null || dbName.isEmpty()) {
            log.info("Could not get environment variable DB_NAME using the default value of 'CatalogDB'");
            dbName = "CatalogDB";
        }

        MongoDatabase db = mc.getDatabase(dbName);


        productCollection = db.getCollection("products");

        // Drop the collection if it exists and then add default content
        productCollection.drop();
        addAll(DEFAULT_PRODUCT_LIST);

    }

    @PreDestroy
    protected void destroy() {
        log.info("Closing MongoClient connection");
        if(mc!=null) {
            mc.close();
        }
    }

    /**
     * This method converts Product POJOs to MongoDB Documents, normally we would place this in a DAO
     * @param product
     * @return
     */
    private Document toDocument(Product product) {
        return new Document()
                .append("itemId",product.getItemId())
                .append("name",product.getName())
                .append("desc",product.getDesc())
                .append("price",product.getPrice());
    }

    /**
     * This method converts MongoDB Documents to Product POJOs, normally we would place this in a DAO
     * @param document
     * @return
     */
    private Product toProduct(Document document) {
        Product product =  new Product();
        product.setItemId(document.getString("itemId"));
        product.setName(document.getString("name"));
        product.setDesc(document.getString("desc"));
        product.setPrice(document.getDouble("price"));
        return product;
    }



    private static List<Product> DEFAULT_PRODUCT_LIST = new ArrayList<>();
    static {
        DEFAULT_PRODUCT_LIST.add(new Product("329299", "Premium Laptop - Developer", "Provides a large display and is physically larger than a Standard Laptop. Includes 1 power adapter. Various peripherals available at an additional cost.", 34.99));
        DEFAULT_PRODUCT_LIST.add(new Product("329199", "Forge Laptop Sticker", "A virtual private network also known as a VPN is a private network that extends across a RBC network. It enables employees to send and receive data across shared or public networks as if their computing devices were directly connected to the RBC network.", 8.50));
        DEFAULT_PRODUCT_LIST.add(new Product("165613", "Solid Performance Polo", "Citrix Receiver is the easy-to-install client software that provides access to your XenDesktop and XenApp installations. With this download you can access applications, desktops and data easily and securely from any device, including smartphones, tablets, PCs and Macs.", 17.80));
        DEFAULT_PRODUCT_LIST.add(new Product("165614", "Ogio Caliber Polo", "This is a Single Function Color network printer with a recommended monthly volume of 2,501 - 10,000 and print speed up to 40 ppm for color/black. Designed to sit on a table/desktop and features a 4.3 color touchscreen control panel.", 28.75));
        DEFAULT_PRODUCT_LIST.add(new Product("165954", "16 oz. Vortex Tumbler", "This is a wide-screen monitor with a 21.5” display.RBC supports the following models: B2240, S22C650, S22A650 or equivalent.This monitor is billed by a one-time expense", 6.00));
        DEFAULT_PRODUCT_LIST.add(new Product("444434", "Pebble Smart Watch", "Provides person-to-person instant messaging, audiovideo calls and file transfer. Does NOT include DesktopApplication sharing or conferencing features.", 24.00));
        DEFAULT_PRODUCT_LIST.add(new Product("444435", "Oculus Rift", "Microsoft Project can be used to assign resources, lay out plans, track phases of projects, analyze workloads, and control budgets. It is useful for managing the progress of small and large projects.Capital Markets US Firm issued mobile devices Mobile Devices are intended for employees who require mobile access to carry out company business on behalf of the Firm.", 106.00));
        DEFAULT_PRODUCT_LIST.add(new Product("444436", "Lytro Camera", "Get the performance driven results of BlackBerry® 10, with the classic navigation keys and physical keyboard you know and love. It’s the smartphone phone you trust with the power you couldn’t imagine.", 44.30));

    }

}
