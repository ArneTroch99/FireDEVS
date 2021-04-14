//-----------------------------------------------------------------------------
// Title       : PhoneCallInterrupter.java for Homework Assignment #4
// Assignment  : Homework #4
// Author      : Robert Firl
// Course      : CSE 561 Modeling & Simulation Theory and Application
// Professor   : Hessam S. Sarjoughian
// Due         : Tuesday March 25, 2013, before 8:00 am AZ time
//-----------------------------------------------------------------------------
// File        : PhoneCallInterrupter.java
//-----------------------------------------------------------------------------
//
// Description : Exercise 1 a 
//               Simplified cell phone model in DEVS-Suite 
//               The ViewableAtomic class is the parent of this class.
//
//-----------------------------------------------------------------------------


package Component.OddsAndEnds;

import model.modeling.message;
import GenCol.entity;
import GenCol.intEnt;
import view.modeling.ViewableAtomic;
import java.util.*;

import Component.SISO.*;

public class PhoneCallInterrupter extends ViewableAtomic
{

   private static final String ON_OFF="OnOff";
   private static final String INCOMING_VOICE="incomingVoice";
   private static final String CALLEE_RESP="calleeResponse";
   private static final String DURATION="duration";
   
   private static final String OUTPUTPORT="outcomingVoice";
   
   
   private static final String OFF="off";
   private static final String ON="on";
   private static final String SEARCHING_4_SVC="searchingForService";
   private static final String ACTIVE="active";
   private static final String READY="ready";
   private static final String FIFO_ADD="fifoAdd";
   private static final String RESPOND="respond";
   
   private static final String START="start";
   private static final String STOP="stop";
   private static final int VALID_EVENT_LEN = 2;
   private static final int SVC_COUNT_LIMIT = 2;
   private static final double SVC_TIME_MIN = 2;
   
   private double stepTime;
   private double timeLeft;
   private String inValue;
   private char inValueC;
   private int inValueI;
   private String outValue;
   private boolean svcAvailability;
   private double svcE1, svcE2;
   private int serviceCount;
   private ArrayDeque<String> theFifo;
   
   //
   // Declare and setup input and ouptut ports, plus values to select from
   //
   public PhoneCallInterrupter(String name)
   {
      super(name);
      inValue = "";
      inValueC = '`';
      inValueI = 1;
      outValue = "NO OUTPUT";
      svcAvailability = false;
      serviceCount=0;
      theFifo = new ArrayDeque<String>();
 
      addInport(ON_OFF);
      addInport(INCOMING_VOICE);
      addInport(CALLEE_RESP);
      addInport(DURATION);
      
      addOutport(OUTPUTPORT);
      addTestInput(ON_OFF, new entity(START));
      addTestInput(ON_OFF, new entity(STOP));
      addTestInput(CALLEE_RESP, new entity("A2"));
      addTestInput(CALLEE_RESP, new entity("B2"));
      addTestInput(CALLEE_RESP, new entity("C2"));
      addTestInput(CALLEE_RESP, new entity("D2"));
      addTestInput(INCOMING_VOICE, new entity("a1"));
      addTestInput(INCOMING_VOICE, new entity("b1"));
      addTestInput(INCOMING_VOICE, new entity("c1"));
      addTestInput(INCOMING_VOICE, new entity("d1"));
      addTestInput(DURATION, new entity("0"), 0);
      addTestInput(DURATION, new entity("0.1"), 0.1);
      addTestInput(DURATION, new entity("1"), 1.0);
      addTestInput(DURATION, new entity("2"), 2.0);
      addTestInput(DURATION, new entity("5.3"), 5.3);
      addTestInput(DURATION, new entity("10"), 10.0);
      addTestInput(DURATION, new entity("13.7"), 13.7);
      
   }

   //
   // Call the main centralized constructor above
   //
   public PhoneCallInterrupter()
   {
      this("PhoneCallInterrupter");
   }
   
   
   //
   // Place model into phase OFF, and wait for input event ON
   //
   public void initialize() {
      super.initialize();

      //
      // stepTime is always set in extDelta, but why chance it
      //
      stepTime = 10.0; 
      timeLeft = 0.0;
      phase = OFF;
      sigma = INFINITY;
      
   }

   //
   // Toggle service availability when this is called.
   //
   private void toggleSvcAvailability()
   {
      svcAvailability = !svcAvailability;
   }
   
   //
   // Determine if the passed input string is valid for incomingVoice
   // Enforces sequential input
   //
   private boolean ValidIncoming(String value)
   {
      char tempC = ' ';;
      int tempI = -1;
      boolean bOut = false;
      if (!theFifo.isEmpty())
      {
         String last = theFifo.peekLast();
         tempC = last.charAt(0);
         tempI = Integer.parseInt(last.substring(1,2));
      }
      else if (phaseIs(ACTIVE))
      {
         tempC = inValueC;
         tempI = inValueI;
      }
      else if (phaseIs(READY) || theFifo.isEmpty())
      {
         tempC = inValueC;
         tempI = inValueI;
      }
      
      if (value.length() == VALID_EVENT_LEN)
      {
         char valC = value.charAt(0);
         int valI = Integer.parseInt(value.substring(1, 2));
      
         if (valC == (char)(tempC + 1) && valI == tempI)
            bOut = true;
      }
      
      return bOut;
   }
   
   //
   // Determine if the passed input string is valid for incomingVoice
   // Enforces sequential input
   //
   private boolean ValidOutgoing(String value)
   {
      String tempC = String.valueOf(inValueC);
      int tempI = inValueI;
      boolean bOut = false;
      StringBuffer sb = new StringBuffer(value);
      String subC = tempC.substring(0,1).toUpperCase();
      String subS = sb.substring(0,1);
      
      if (value.length() == VALID_EVENT_LEN)
      {
         if (subC.equals(subS))
            if ((tempI + 1) == Integer.parseInt(value.substring(1,2)))
               bOut = true;
      }
      
      return bOut;
   }
   
   
   
   //
   // Obtain the stepTime from the Duration input
   // Perform the phase transition per the details described in HW 4
   // Off ===> On ===> SearchingForService ====> Ready ====> Active ====>
   // add to FIFO (if Active phase) ====> SearchingForService on outcomingVoice
   // Set to Off from any phase if STOP input on OnOff input.
   //
   public void deltext(double e, message x) 
   {
      for (int i = 0; i < x.getLength(); i++) 
      {
         if (messageOnPort(x, DURATION, i))
         {
            entity eStep = x.getValOnPort(DURATION,i);
            stepTime = Double.parseDouble(eStep.toString());
            break;
         }
      }
      
      for (int i = 0; i < x.getLength(); i++) 
      {
         if (messageOnPort(x, ON_OFF, i))
         {
            entity input = x.getValOnPort(ON_OFF,i);
            if(input.eq(START) && phaseIs(OFF))
            {
               holdIn(ON, stepTime);
               return;
            } 
            else if (input.eq(STOP))
            {
               timeLeft = sigma - e;
               theFifo.clear();
               if (timeLeft > 0)
               {
                  holdIn(OFF, 0);
                  return;
               }
               else
               {
                  holdIn(OFF, stepTime);
                  return;
               }
            } 
         }
      }
      
      if (phaseIs(ON))
      {
         holdIn(SEARCHING_4_SVC, stepTime);
         return;
      }
      else if (phaseIs(SEARCHING_4_SVC))
      {
         if (serviceCount == 0)
            svcE1 = stepTime;
         else
            svcE2 = svcE2 + stepTime;
         serviceCount++;
         if (serviceCount >= SVC_COUNT_LIMIT && (svcE2 - svcE1) >= SVC_TIME_MIN)
         {
            serviceCount = 0;
            if (svcAvailability)
            {
               holdIn(READY, stepTime);
               return;
            }
            else 
            {
               holdIn(ON, stepTime);
               return;
            }
         }
      }
      
      for (int i = 0; i < x.getLength(); i++) 
      {
         if (messageOnPort(x, INCOMING_VOICE, i))
         {
            entity input = x.getValOnPort(INCOMING_VOICE, i);
            String inp = input.toString();
            if (phaseIs(READY) && ValidIncoming(inp))
            {
               inValueC = inp.charAt(0);
               inValueI = Integer.parseInt(inp.substring(1, 2));
               holdIn(ACTIVE, stepTime);
               return;
            }
            else if (phaseIs(ACTIVE) && ValidIncoming(inp))
            {
               timeLeft = sigma - e;
               theFifo.addLast(inp);
               inValueC = inp.charAt(0);
               inValueI = Integer.parseInt(inp.substring(1, 2));
               holdIn(FIFO_ADD, stepTime);
               return;
            } 
         }
      }
      
      for (int i = 0; i < x.getLength(); i++) 
      {
         if (messageOnPort(x, CALLEE_RESP, i))
         {
            entity input = x.getValOnPort(CALLEE_RESP,i);
            String inp = input.toString();
            if (phaseIs(ACTIVE) && ValidOutgoing(inp))
            {
               outValue = inp;
               holdIn(RESPOND, stepTime);
               return;
            } 
         }
      }
      
     
   } // end deltext

   //
   // Always toggle service availability
   // Check for FIFOAdd phase and return to Active
   // else if phase is Respond, look at the FIFO for being empty, set phase 
   // to SearchingForService, otherwise get the oldest element from the FIFO
   // set phase to Active immediately
   // else if timeLeft is positive, remain in the current phase, otherwise
   // passivate
   //
   public void deltint() 
   {
      toggleSvcAvailability();
      
      if (phaseIs(FIFO_ADD))
      {
         if (timeLeft > 0)
            holdIn(ACTIVE, timeLeft);
         else
            holdIn(ACTIVE, stepTime);
      }
      else if(phaseIs(RESPOND))
      {
         if (theFifo.isEmpty())
            holdIn(SEARCHING_4_SVC, stepTime);
         else 
         {
            inValue = theFifo.pollFirst();
            holdIn(ACTIVE, 0);
         }
      }
      else if (timeLeft > 0)
      {
         holdIn(phase, timeLeft);
      }
      else
         passivateIn(phase);
   }


   //
   // Confluence transition function. Generate output, call internal transition
   // then external transition
   //
   public void deltcon(double e, message x) 
   {
      deltint();
      deltext(0, x);
   }

   //
   // Send the output when in Respond phase, otherwise output is 
   // special to indicate no output.  The coding content below is 
   // based on the Homework 3 solution for CSE 561
   //
   public message out() {
      message m = new message();
      if (phaseIs(RESPOND))
      {
         System.out.println("Output = " + outValue);
         m.add(makeContent(OUTPUTPORT, new entity(outValue)));
      }
      else 
      {
         System.out.println("Output = NO OUTPUT");
         m.add(makeContent(OUTPUTPORT, new entity("NO OUTPUT")));
      }

      return m;
   }

   public String getTooltipText(){
      return
            super.getTooltipText()
            + "\n" + "InComing: " + inValue 
            + "\n" + "OutComing: " + outValue
            + "\n" + "Time Left: " + timeLeft;
   }

   
} // end PhoneCallInterrupter
