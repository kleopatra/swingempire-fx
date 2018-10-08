/*
 * Created on 08.10.2018
 *
 */
package de.swingempire.fx.scene.control.text;

import java.text.ChoiceFormat;
import java.text.MessageFormat;
/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * from java tutorial
 * https://docs.oracle.com/javase/tutorial/i18n/format/messageFormat.html
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class MessageFormatDemo {

   void displayMessage(Locale currentLocale) {

      System.out.println("currentLocale = " + currentLocale.toString());
      System.out.println();

//      URL res = getClass().getResource("messagebundle.properties", currentLocale);
      
      LOG.info("res: " + getClass().getPackageName());
      ResourceBundle messages = 
         ResourceBundle.getBundle(getClass().getPackageName() +".messagebundle", currentLocale);

      System.out.println("bundle: " + messages);
      Object[] messageArguments = {
         messages.getString("planet"),
         new Integer(7),
         new Date()
      };

      MessageFormat formatter = new MessageFormat("");
      formatter.setLocale(currentLocale);

      formatter.applyPattern(messages.getString("template"));
      String output = formatter.format(messageArguments);

      System.out.println(output);
      
      int planet = 7;
      String event = "a disturbance in the Force";

      String result = MessageFormat.format(
          "At {1,time} on {1,date}, there was {2} on planet {0,number,integer}.",
          planet, new Date(), event);
      System.out.println(result);
      
      ObservableList<String> items = FXCollections.observableArrayList("one", "tow");
      
//      String result2 = MessageFormat.format("The list contains {0, number, integer} item(s)", items.size());
//      System.out.println(result2);
      
      MessageFormat itemsFormatter = new MessageFormat("");
      Object[] itemsArgs = {
              items.size(),
              messages.getString("items")
      };
      itemsFormatter.applyPattern(messages.getString("itemstemplate"));
      String itemsOut = itemsFormatter.format(itemsArgs);
      System.out.println(itemsOut);
      
      //----------- plain choiceFormat
      
      MessageFormat form = new MessageFormat("The disk \"{1}\" contains {0}.");
      double[] filelimits = {0,1,2};
      String[] filepart = {"no files","one file","{0,number} files"};
      ChoiceFormat fileform = new ChoiceFormat(filelimits, filepart);
      form.setFormatByArgumentIndex(0, fileform);
      
      int fileCount = 1273;
      String diskName = "MyDisk";
      Object[] testArgs = {new Long(fileCount), diskName};

      System.out.println(form.format(testArgs));
      

      form.applyPattern("The list contains {0}");
      double[] limits = {0, 1, 2};
      String[] parts = {"keine Elemente", "ein Element", "{0, number} Elemente"};
      ChoiceFormat listForm = new ChoiceFormat(limits, parts);
      form.setFormatByArgumentIndex(0, listForm);
      System.out.println("direct: " + form.format(new Object[]{items.size()}));
      
      form.applyPattern(messages.getString("choicetemplate"));
      parts = new String[]{messages.getString("noitems"), messages.getString("oneitem"), messages.getString("moreitems")};
      ChoiceFormat choice = new ChoiceFormat(limits, parts);
      form.setFormat(0, choice);
      System.out.println("from bundle: " + form.format(new Object[]{items.size()}));
//      form.format(new Object[] {items.size()});
   
   }

   static public void main(String[] args) {
       MessageFormatDemo demo = new MessageFormatDemo();
      demo.displayMessage(new Locale("en", "US"));
      System.out.println();
      demo.displayMessage(new Locale("de", "DE"));
   }
   
   @SuppressWarnings("unused")
private static final Logger LOG = Logger
        .getLogger(MessageFormatDemo.class.getName());
} 
