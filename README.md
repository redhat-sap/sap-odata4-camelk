Camel K SAP demo

This demo uses the Classic Enterprise Procurement module in Central Component  of SAP ERP (SAP ECC) and gets the sales order details using Telegram Messenger. An employee entering the order id to the chatbot on Telegram, it will immediately receive the order result back in the same conversation screen. 


Setup your ENV

- Have OpenShift Platform with Administrator right
- Install Knative Serving & Eventing (Cluster Wide)
- Install Camel K Operator (Cluster Wide)
- Install Camel K binary or Camel K extension in VS Code
- Create an Telegram bot and get the authorization token (https://core.telegram.org/bots)


1. Create a project to run the integration code

```
oc new-project sapdemo
```

2. Create the channel needed

In Channel folder

```
oc create -f channel/channel-ordercheck.yaml
oc create -f channel/channel-returntxt.yaml
```

Telegram 
(in telegram folder)
--------------
3. Create the Camel K Application that integrate with SAP

```
kamel run SapOdata.java
```

4. Add the Telegram authorization token to sink.properties and install the function that connects to Telegram

```
kamel run SapSales.yaml --property-file=sink.properties
```

5. Install the telegram Kamelet if not already avalible. 

```
oc create -f telegram-source.kamelet.yaml
```

6. Create the Kamelet Binding. First replace the Telegram authorization token with yours

```
oc create -f telegram-binding.yaml
```
--------------


Slack
(in slack folder)
--------------
3. Create the Camel K Application that integrate with SAP

```
kamel run SapOdata.java
```

4. Activate Incoming Webhooks in slack bot, and copy the Webhook URL add to sinkslack.properties


```
kamel run SapSalesSlack.yaml --property-file=sinkslack.properties
```

5. Install the Slack Kamelet if not already avalible. 

```
oc create -f slack-source.kamelet.yaml
```

6. Create the Kamelet Binding. First replace the  authorization token with your Slack Bot User OAuth Token

```
oc create -f slack-binding.yaml
```
--------------


DONE
