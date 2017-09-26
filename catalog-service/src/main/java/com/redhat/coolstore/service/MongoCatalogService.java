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
        DEFAULT_PRODUCT_LIST.add(new Product("329299", "Premium Laptop - Developer", "Provides a large display and ", 870));
        DEFAULT_PRODUCT_LIST.add(new Product("329199", "Create VPN Access", "A virtual private network also ", 0.00));
        DEFAULT_PRODUCT_LIST.add(new Product("165613", "Citrix Client", "Citrix Receiver is the easy-to-install client ", 0.80));
        DEFAULT_PRODUCT_LIST.add(new Product("165614", "HP Color LaserJet Ent M553x - USWM", "This is a Single Function ", 1200.60));
        DEFAULT_PRODUCT_LIST.add(new Product("165954", "22 inch Monitor, One-Time Expense - USWM", "This is a wide-screen monitor with a ", 6.00));
        DEFAULT_PRODUCT_LIST.add(new Product("444434", "Lync (Skype)", "Provides person-to-person instant messaging, audiovideo calls and", 0.00));
        DEFAULT_PRODUCT_LIST.add(new Product("444435", "Microsoft Project Professional 2016", "Microsoft Project can be used to ", 0.00));
        DEFAULT_PRODUCT_LIST.add(new Product("444436", "Blackberry Classic - Provided by Verizon - CM US", "Get the performance driven results of ", 456.30));

    }

}
