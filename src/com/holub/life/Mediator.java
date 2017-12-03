package com.holub.life;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.holub.io.Files;
import com.holub.ui.MenuSite;

import java.awt.*;

public class Mediator {
	public Clock clock;
	public Universe universe;
	public Operate operate;
	public static Mediator instance;
	
	public Mediator(){
		universe = Universe.instance();
		operate = new Operate();
		clock = Clock.instance();
	}
	
	public static synchronized Mediator instance(){
		if( instance == null )
			instance = new Mediator();
		return instance;
	}
	
	public void setMenu_universe(){
		operate.callAddLine(this, "Grid", "Clear");
		operate.callAddLine(this, "Grid", "Load");
		operate.callAddLine(this, "Grid", "Store");
		operate.callAddLine(this, "Grid", "Exit");
	}
	public void setMenu_Clock(){
		ActionListener modifier =									//{=startSetup}
				new ActionListener()
				{	public void actionPerformed(ActionEvent e)
					{
						String name = ((JMenuItem)e.getSource()).getName();
						char toDo = name.charAt(0);

						if( toDo=='T' )
							clock.tick();				      // single tick
						else
							clock.startTicking(   toDo=='A' ? 500:	  // agonizing
											toDo=='S' ? 150:	  // slow
											toDo=='M' ? 70 :	  // medium
											toDo=='F' ? 30 : 0 ); // fast
					}
				};
																		// {=midSetup}
			MenuSite.addLine(this,"speed","Stop",  			modifier);
			MenuSite.addLine(this,"speed","Tick (Single Step)",modifier);
			MenuSite.addLine(this,"speed","Agonizing",	 	  	modifier);
			MenuSite.addLine(this,"speed","Slow",		 		modifier);
			MenuSite.addLine(this,"speed","Medium",	 	 	modifier);
			MenuSite.addLine(this,"speed","Fast",				modifier); // {=endSetup}
	}
	public interface Methods{
		void doStore();
		void doLoad();
	}
	public class MethodImplements implements Methods{
		
		public void doStore()
		{	try
			{
				FileOutputStream out = new FileOutputStream(Files.userSelected(".",".life","Life File","Write"));

				Clock.instance().stop();		// stop the game

				Storable memento = universe.outermostCell.createMemento();
				universe.outermostCell.transfer( memento, new Point(0,0), Cell.STORE );
				memento.flush(out);

				out.close();
			}
			catch( IOException theException )
			{	JOptionPane.showMessageDialog( null, "Write Failed!",
						"The Game of Life", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		public void doLoad()
		{	try
			{
				FileInputStream in = new FileInputStream(
				   Files.userSelected(".",".life","Life File","Load"));

				Clock.instance().stop();		// stop the game and
				universe.outermostCell.clear();			// clear the board.

				Storable memento = universe.outermostCell.createMemento();
				memento.load( in );
				universe.outermostCell.transfer( memento, new Point(0,0), Cell.LOAD );

				in.close();
			}
			catch( IOException theException )
			{	JOptionPane.showMessageDialog( null, "Read Failed!",
						"The Game of Life", JOptionPane.ERROR_MESSAGE);
			}
		universe.repaint();
		}
		
	}
	private class Operate extends MethodImplements{
		public Operate(){}
		public void callAddLine(Object object, String type, String operateType) {
			MenuSite.addLine
			(	object, type, operateType,new ActionListener(){	
				public void actionPerformed(ActionEvent e)
			        {	
						if(operateType=="Clear") {
							universe.repaint();
						}
						else if(operateType=="Load") {
							doLoad();
						}
						else if(operateType=="Store") {
							doStore();
							
						}
						else if(operateType=="Exit") {
							System.exit(0);
						}
						
			        }
				}
			);
		}
	}
}
