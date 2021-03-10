// camel-k: language=java property-file=sap.properties 

import org.apache.camel.builder.RouteBuilder;

import java.util.Map;
import java.util.HashMap;

public class SapOdata extends RouteBuilder {

  Map<String, String> tempMap = new HashMap<String, String>();
    

  @Override
  public void configure() throws Exception {

  from("knative:channel/ordercheck")
    .unmarshal().json()
    .setHeader("CamelOlingo4.keyPredicate", simple("'${body[text]}'"))
    .bean(this, "create(\"${body[text]}\",\"${body[chat][id]}\")")
    .to("olingo4://read/SalesOrder")
    .split(simple("${body.properties}"))
      .choice()
        .when(simple("${body.name} == 'Customer' "))
          .bean(this, "aggregate(\"CUSTOMER\",'${body.value}')")
        .when(simple("${body.name} == 'Transactioncurrency' "))
          .bean(this, "aggregate(\"CURRENCY\",'${body.value}')")
        .when(simple("${body.name} == 'Grossamountintransaccurrency' "))
          .bean(this, "aggregate(\"SUM\",'${body.value}')")
        .when(simple("${body.name} == 'Taxamountintransactioncurrency' "))
          .bean(this, "aggregate(\"PRICE\",'${body.value}')")
              //.marshal().json().log("${body}")
      .end()
    .end()
    .setBody(method(this, "getSalesOrder()"))
    .marshal().json()
    .log("${body}")
    .to("knative://channel/returntxt")
  ;

  
    
    

  
/* 
      // Write your routes here, for example:
      from("timer:java?period=1000&repeatCount=01").routeId("SalesOrder")
        .setHeader("CamelOlingo4.$top").constant("10")
        .to("olingo4://read/SalesOrder")
        .split(simple("${body.entities}"))
           .log("NEW SALES ORDER------->")
           .split(simple("${body.properties}"))
              .choice()
                .when(simple("${body.name} == 'Salesorder' "))
                  .log("SALES ORDER : ${body.value}")
                .when(simple("${body.name} == 'Customer' "))
                  .log("CUSTOMER : ${body.value}")
                .when(simple("${body.name} == 'Transactioncurrency' "))
                  .log("CURRENCY : ${body.value}")
                .when(simple("${body.name} == 'Grossamountintransaccurrency' "))
                  .log("SUM : ${body.value}")
                .when(simple("${body.name} == 'Taxamountintransactioncurrency' "))
                  .log("PRICE : ${body.value}")
            .end()
        .end()
      ;
    
      

      from("timer:java?period=1000&repeatCount=01").routeId("Select single SalesOrder")
        .to("olingo4://read/SalesOrder('500000010')")
        
          .log("SELECTED SALES ORDER------->")
          .split(simple("${body.properties}"))
              .choice()
                .when(simple("${body.name} == 'Salesorder' "))
                  .log("SALES ORDER : ${body.value}")
                .when(simple("${body.name} == 'Customer' "))
                  .log("CUSTOMER : ${body.value}")
                .when(simple("${body.name} == 'Transactioncurrency' "))
                  .log("CURRENCY : ${body.value}")
                .when(simple("${body.name} == 'Grossamountintransaccurrency' "))
                  .log("SUM : ${body.value}")
                .when(simple("${body.name} == 'Taxamountintransactioncurrency' "))
                  .log("PRICE : ${body.value}")
              //.marshal().json().log("${body}")
          .end()
      ;

      from("timer:java?period=1000&repeatCount=01").routeId("SalesOrderItem")
        .setHeader("CamelOlingo4.$top").constant("1")
        .to("olingo4://read/SalesOrderItem")
        .split(simple("${body.entities}"))
           .log("NEW SALES ITEM ORDER------->")
           .split(simple("${body.properties}"))
              .log("${body.name} : ${body.value}")
              //.marshal().json().log("${body}")
        .end()
      ;

      */

  }

  public void create(String salesorder, String chatid) {

    tempMap= new HashMap<String, String>();
    tempMap.put("SALESORDER", salesorder);
    tempMap.put("CHATID", chatid);
    
  }


  public void aggregate(String name, String value) {
    tempMap.put(name, value);
    
  }

  public Map<String, String> getSalesOrder() {
    
    return tempMap;
  }
}
