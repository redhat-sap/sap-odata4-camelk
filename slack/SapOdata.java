// camel-k: language=java property-file=sap.properties 

import org.apache.camel.builder.RouteBuilder;

import java.util.Map;
import java.util.HashMap;

public class SapOdata extends RouteBuilder {

  Map<String, String> tempMap = new HashMap<String, String>();
    

  @Override
  public void configure() throws Exception {

  from("knative:channel/ordercheck")
    .log("FROM EVENTING --- [${body}]")
    .unmarshal().json()
    .setHeader("CamelOlingo4.keyPredicate", simple("'${body[text]}'"))
    .choice()
      .when(header("CamelOlingo4.keyPredicate").regex("\'\\d{9}\'"))
        .log("validate OK")
        .to("direct:runsap")
    .end()
    
    
  ;

  from("direct:runsap")
    .bean(this, "create(\"${body[text]}\")")
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
      .end()
    .end()
    .setBody(method(this, "getSalesOrder()"))
    .marshal().json()
    .log("${body}")
    .to("knative://channel/returntxt")
  ;

  from("direct:incorrect")
    .setBody().constant("{\"error\":\"UNKNOWN SALESORDER FORMAT!!\"}")
    .log("${body}")
  ;  
    

  }

  public void create(String salesorder) {

    tempMap= new HashMap<String, String>();
    tempMap.put("SALESORDER", salesorder);
    
  }


  public void aggregate(String name, String value) {
    tempMap.put(name, value);
    
  }

  public Map<String, String> getSalesOrder() {
    
    return tempMap;
  }
}
