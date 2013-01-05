/*
 *  java-calendar - a java calendar for Germany
 *  Copyright (C) 2012  Johannes Steltzer
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.jsteltze.calendar.frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicLabelUI;

import org.apache.log4j.Logger;

import de.jsteltze.calendar.Calendar;
import de.jsteltze.calendar.Event;
import de.jsteltze.calendar.Frequency;
import de.jsteltze.calendar.Update;
import de.jsteltze.calendar.XMLParser;
import de.jsteltze.calendar.config.ColorSet;
import de.jsteltze.calendar.config.Configuration;
import de.jsteltze.calendar.config.Const;
import de.jsteltze.calendar.config.Holidays;
import de.jsteltze.calendar.exceptions.CannotParseException;
import de.jsteltze.common.ColorChooser;
import de.jsteltze.common.ColorChooserListener;
import de.jsteltze.common.LinkLabel;
import de.jsteltze.common.Music;
import de.jsteltze.common.VerticalFlowPanel;

/**
 * Settings frame for calendar configuration.
 * @author Johannes Steltzer
 *
 */
public class Settings 
    extends JDialog 
    implements ActionListener, ItemListener, KeyListener, 
        ColorChooserListener {
    
    private static final long serialVersionUID = 1L;
    
    /** parent calendar object */
    private Calendar caller;

    /* Tabs */
    /** tab pane */
    private JTabbedPane tab;
    public static final int TAB_GENERAL = 0;
    public static final int TAB_HOLIDAYS = 1;
    public static final int TAB_COLORS = 2;
    public static final int TAB_IMEXPORT = 3;
    public static final int TAB_INFO = 4;
    
    private static final String defaultButtonText = "Standardwerte verwenden";

    /* For Tab1: "Allgemein" */
    private JComboBox remindBox, onCloseBox, styleBox, 
            onClickDayBox, onClickEventBox;
    private JCheckBox autoUpdateBox, moonBox, ownThemeBox, 
            systrayBox, playThemeBox, buttonTextsBox;
    private JLabel ownThemeLabel;
    private JButton playThemeButton, chooseButton;
    private byte initUI, currentUI;

    /* For Tab2: "Feiertage" */
    private JCheckBox[] holidayBoxes, specialDayBoxes;

    /* For Tab3: "Farben" */
    private JRadioButton[] colorRadioButton;
    private JTextField[] colorField;
    private JLabel[] colorRects;
    private ColorChooser colorChooser;

    /* For Tab4: "Import/Export" */
    private JCheckBox[] importExportBoxes;
    private JCheckBox[] allImportExportBoxes;
    private int numConfig, numEvents;
    private JScrollPane scrollPane;
    private JPanel exportPanel;
    private JLabel titleLabel;
    private JRadioButton importButton, exportButton;
    private JLabel importExportLabel;
    private Vector<Event> possibleEvents;
    private Configuration possibleConfig;
    private JButton fileButton;
    
    /* For Tab5: "Programminfo" */
    private JButton updateButton;
    
    /* For all tabs */
    private JButton okButton, cancelButton, 
            defaultButtonTab1, defaultButtonTab2, defaultButtonTab3;
    
    private static Logger logger = Logger.getLogger(Settings.class);

    /**
     * Sets the icon of this frame.
     */
    private void setIcon() {
        URL url = this.getClass().getClassLoader().getResource("media/calendar32.ico");
        Image ima = Toolkit.getDefaultToolkit().createImage(url);
        setIconImage(ima);
    }

    /**
     * Arrange tab 1: "Allgemein"
     */
    private JPanel arrangeTab1() {
        remindBox = new JComboBox();
        for (int i = 0; i < Event.NUMBER_REMINDS; i++)
            remindBox.addItem(Event.getReminderAsString(i, false));
        remindBox.setSelectedIndex(caller.getConfig().getReminder());
        onCloseBox = new JComboBox();
        onCloseBox.addItem(Configuration.ON_CLOSE_LABELS[Configuration.ON_CLOSE_EXIT]);
        onCloseBox.addItem(Configuration.ON_CLOSE_LABELS[Configuration.ON_CLOSE_MOVE_TO_SYSTRAY]);
        onCloseBox.setSelectedIndex(caller.getConfig().getOnCloseAction());
        onClickDayBox = new JComboBox();
        onClickDayBox.addItem(Configuration.ON_CLICK_DAY_LABELS[Configuration.ON_CLICK_DAY_OVERVIEW]);
        onClickDayBox.addItem(Configuration.ON_CLICK_DAY_LABELS[Configuration.ON_CLICK_DAY_NEW]);
        onClickDayBox.addItem(Configuration.ON_CLICK_DAY_LABELS[Configuration.ON_CLICK_DAY_NONE]);
        onClickDayBox.setSelectedIndex(caller.getConfig().getOnClickDayAction());
        onClickEventBox = new JComboBox();
        onClickEventBox.addItem(Configuration.ON_CLICK_EVENT_LABELS[Configuration.ON_CLICK_EVENT_REMIND]);
        onClickEventBox.addItem(Configuration.ON_CLICK_EVENT_LABELS[Configuration.ON_CLICK_EVENT_EDIT]);
        onClickEventBox.addItem(Configuration.ON_CLICK_EVENT_LABELS[Configuration.ON_CLICK_EVENT_NONE]);
        onClickEventBox.setSelectedIndex(caller.getConfig().getOnClickEventAction());
        styleBox = new JComboBox();
        styleBox.addItem(Configuration.STYLE_LABELS[Configuration.STYLE_SYSTEM]);
        styleBox.addItem(Configuration.STYLE_LABELS[Configuration.STYLE_SWING]);
        styleBox.addItem(Configuration.STYLE_LABELS[Configuration.STYLE_MOTIF]);
        styleBox.addItem(Configuration.STYLE_LABELS[Configuration.STYLE_NIMBUS]);
        
        initUI = currentUI = caller.getConfig().getStyle();
        styleBox.setSelectedIndex(caller.getConfig().getStyle());
        styleBox.addItemListener(this);
        
        autoUpdateBox = new JCheckBox("Automatisch nach Updates suchen");
        autoUpdateBox.setSelected(caller.getConfig().getAutoUpdate());
        
        moonBox = new JCheckBox("Mondphasen anzeigen");
        moonBox.setSelected(caller.getConfig().getMoon());
        
        buttonTextsBox = new JCheckBox("Button-Texte anzeigen");
        buttonTextsBox.setSelected(caller.getConfig().getButtonTexts());
        
        systrayBox = new JCheckBox("Im Systray starten");
        systrayBox.setSelected(caller.getConfig().getSystrayStart());
        
        playThemeBox = new JCheckBox("Abspielmusik:");
        playThemeBox.setSelected(caller.getConfig().getPlayTheme());
        playThemeBox.addItemListener(this);
                
        ownThemeBox = new JCheckBox("eigene");
        ownThemeBox.setEnabled(playThemeBox.isSelected());
        chooseButton = new JButton("...");
        chooseButton.setToolTipText("Datei auswählen");
        chooseButton.setMargin(new Insets(3, 2, 3, 2));
        chooseButton.addActionListener(this);
        if (caller.getConfig().getTheme() == Configuration.defaultConfig.getTheme()) {
            ownThemeBox.setSelected(false);
            ownThemeLabel = new JLabel("");
            ownThemeLabel.setToolTipText("");
            chooseButton.setEnabled(false);
        }
        else {
            ownThemeBox.setSelected(true);
            String path = caller.getConfig().getTheme();
            ownThemeLabel = new JLabel(path);
            ownThemeLabel.setToolTipText(path);
            chooseButton.setEnabled(playThemeBox.isSelected());
        }
        ownThemeBox.addItemListener(this);
        ownThemeLabel.setForeground(Color.GRAY);
        playThemeButton = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("media/play20.ico")));
        playThemeButton.setMargin(new Insets(0, 2, 0, 2));
        playThemeButton.addActionListener(this);
        playThemeButton.setToolTipText("Melodie abspielen");
        playThemeButton.setEnabled(playThemeBox.isSelected());

        defaultButtonTab1 = new JButton(defaultButtonText);
        defaultButtonTab1.addActionListener(this);

        JPanel pAll = new JPanel(new BorderLayout());
        VerticalFlowPanel pMain = new VerticalFlowPanel();
        
        JPanel p1 = new JPanel(new GridLayout(2, 1));
        JPanel p11 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JPanel p12 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        
        JPanel p2 = new JPanel(new GridLayout(3, 1));
        JPanel p21 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JPanel p22 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JPanel p23 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        
        JPanel p3 = new JPanel(new GridLayout(6, 1));
        JPanel p31 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JPanel p32 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JPanel p33 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JPanel p34 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JPanel p35 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JPanel p36 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        JPanel pC = new JPanel(new GridLayout(1, 1));
        JPanel pS = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        p11.add(new JLabel(" Standard-Abstand: "));
        p11.add(remindBox);
        p12.add(playThemeBox);
        p12.add(ownThemeBox);
        p12.add(chooseButton);
        p12.add(playThemeButton);
        p12.add(ownThemeLabel);
        p1.add(p11);
        p1.add(p12);
        p1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Erinnerung",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                Const.FONT_BORDER_TEXT));
        
        p21.add(moonBox);
        p22.add(buttonTextsBox);
        p23.add(new JLabel(" Look and Feel: "));
        p23.add(styleBox);
        p2.add(p21);
        p2.add(p22);
        p2.add(p23);
        p2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Anzeige",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                Const.FONT_BORDER_TEXT));
        
        File workingDir = new File(caller.getWorkspace());
        JLabel workingDirLabel = new JLabel(workingDir.getAbsolutePath());
        workingDirLabel.setForeground(Color.GRAY);
        
        p31.add(new JLabel(" Arbeitsverzeichnis: "));
        p31.add(workingDirLabel);
        p32.add(new JLabel(" Aktion beim Schließen des Programms: "));
        p32.add(onCloseBox);
        p33.add(new JLabel(" Aktion beim Klick auf ein Ereignis: "));
        p33.add(onClickEventBox);
        p34.add(new JLabel(" Aktion beim Klick auf einen Tag: "));
        p34.add(onClickDayBox);
        p35.add(autoUpdateBox);
        p36.add(systrayBox);
        
        p3.add(p31);
        p3.add(p32);
        p3.add(p33);
        p3.add(p34);
        p3.add(p35);
        p3.add(p36);
        p3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Allgemein",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                Const.FONT_BORDER_TEXT));
        
        pMain.add(p3);
        pMain.add(p2);
        pMain.add(p1);

        JScrollPane jsp = new JScrollPane(pMain);
        
        pC.add(jsp);
        pC.setBorder(new EmptyBorder(5, 5, 5, 5));
        pC.setBackground(Const.COLOR_SETTINGS_TABS_BG);
        pS.add(defaultButtonTab1);
        pS.setBackground(Const.COLOR_SETTINGS_TABS_BG);
        
        pAll.add(pC, BorderLayout.CENTER);
        pAll.add(pS, BorderLayout.SOUTH);

        pAll.setBackground(Const.COLOR_SETTINGS_TABS_BG);
        return pAll;
    }

    /**
     * Arrange tab 2: "Feiertage"
     */
    private JPanel arrangeTab2() {
        holidayBoxes = new JCheckBox[Holidays.TOTAL_HOLIDAY_LAW];
        holidayBoxes[0] = new JCheckBox("Neujahr (1.1.)");
        holidayBoxes[1] = new JCheckBox("Heilige 3 Könige (6.1.)");
        holidayBoxes[2] = new JCheckBox("Gründonnerstag (variabel)");
        holidayBoxes[3] = new JCheckBox("Karfreitag (variabel)");
        holidayBoxes[4] = new JCheckBox("Ostermontag (variabel)");
        holidayBoxes[5] = new JCheckBox("Tag der Arbeit (1.5.)");
        holidayBoxes[6] = new JCheckBox("Christihimmelfahrt (variabel)");
        holidayBoxes[7] = new JCheckBox("Pfingstmontag (variabel)");
        holidayBoxes[8] = new JCheckBox("Fronleichnam (variabel)");
        holidayBoxes[9] = new JCheckBox("Mariä Himmelfahrt (15.8.)");
        holidayBoxes[10] = new JCheckBox("Tag der deutschen Einheit (3.10.)");
        holidayBoxes[11] = new JCheckBox("Reformationstag (31.10.)");
        holidayBoxes[12] = new JCheckBox("Allerheiligen (1.11.)");
        holidayBoxes[13] = new JCheckBox("Buß- und Bettag (variabel)");
        holidayBoxes[14] = new JCheckBox("1. Weihnachtsfeiertag (25.12.)");
        holidayBoxes[15] = new JCheckBox("2. Weihnachtsfeiertag (26.12.)");
        
        specialDayBoxes = new JCheckBox[Holidays.TOTAL_HOLIDAY_SPECIAL];
        specialDayBoxes[0] = new JCheckBox("Valentinstag (14.2.)");
        specialDayBoxes[1] = new JCheckBox("Rosenmontag (variabel)");
        specialDayBoxes[2] = new JCheckBox("Faschingsdienstag (variabel)");
        specialDayBoxes[3] = new JCheckBox("Aschermittwoch (variabel)");
        specialDayBoxes[4] = new JCheckBox("Frauentag (8.3.)");
        specialDayBoxes[5] = new JCheckBox("Palmsonntag (variabel)");
        specialDayBoxes[6] = new JCheckBox("Muttertag (variabel)");
        specialDayBoxes[7] = new JCheckBox("Kindertag (1.6.)");
        specialDayBoxes[8] = new JCheckBox("Halloween (31.10.)");
        specialDayBoxes[9] = new JCheckBox("Martinstag (11.11.)");
        specialDayBoxes[10] = new JCheckBox("Volkstrauertag (variabel)");
        specialDayBoxes[11] = new JCheckBox("Totensonntag (variabel)");
        specialDayBoxes[12] = new JCheckBox("1. Advent (variabel)");
        specialDayBoxes[13] = new JCheckBox("Nikolaus (6.12.)");
        specialDayBoxes[14] = new JCheckBox("2. Advent (variabel)");
        specialDayBoxes[15] = new JCheckBox("3. Advent (variabel)");
        specialDayBoxes[16] = new JCheckBox("4. Advent (variabel)");
        specialDayBoxes[17] = new JCheckBox("Heiligabend (24.12.)");
        specialDayBoxes[18] = new JCheckBox("Silvester (31.12.)");

        defaultButtonTab2 = new JButton(defaultButtonText);
        defaultButtonTab2.addActionListener(this);

        logger.debug("HOLIDAY ID=" + caller.getConfig().getHolidays());
        
        JPanel pNorth = new JPanel(new GridLayout(8, 2));
        pNorth.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Gesetzliche Feiertage",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                Const.FONT_BORDER_TEXT));
        JPanel pSouth = new JPanel(new GridLayout(10, 2));
        pSouth.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Sonstige Feiertage",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                Const.FONT_BORDER_TEXT));
        
        JPanel pAll = new JPanel(new BorderLayout());
        VerticalFlowPanel pMain = new VerticalFlowPanel();
        JPanel pC = new JPanel(new GridLayout(1, 1));
        JPanel pS = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        for (int i = 0; i < Holidays.TOTAL_HOLIDAY_LAW; i++) {
            holidayBoxes[i].setSelected((caller.getConfig().getHolidays() & (1 << i)) == (1 << i));
            pNorth.add(holidayBoxes[i]);
        }
        for (int i = 0; i < Holidays.TOTAL_HOLIDAY_SPECIAL; i++) {
            specialDayBoxes[i].setSelected((caller.getConfig().getSpecialDays() & (1 << i)) == (1 << i));
            pSouth.add(specialDayBoxes[i]);
        }

        pMain.add(pNorth);
        pMain.add(pSouth);
        JScrollPane jsp = new JScrollPane(pMain, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pC.add(jsp);
        pC.setBorder(new EmptyBorder(5, 5, 5, 5));
        pC.setBackground(Const.COLOR_SETTINGS_TABS_BG);
        pS.add(defaultButtonTab2);
        pS.setBackground(Const.COLOR_SETTINGS_TABS_BG);
        pAll.add(pC, BorderLayout.CENTER);
        pAll.add(pS, BorderLayout.SOUTH);

        pAll.setBackground(Const.COLOR_SETTINGS_TABS_BG);
        return pAll;
    }

    /**
     * Arrange tag 3: "Farben"
     */
    private JPanel arrangeTab3() {
        JPanel pAll = new JPanel(new BorderLayout());
        JPanel pMain = new JPanel(new BorderLayout());
        JPanel pC = new JPanel(new GridLayout(1, 1));
        JPanel pColors = new JPanel(new BorderLayout());
        JPanel pColorChooser = new JPanel(null);
        JPanel pColorTexts = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel pS = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        VerticalFlowPanel pRadiosAll = new VerticalFlowPanel(4);

        colorChooser = new ColorChooser(this, caller.getConfig().getColors()[0]);
        colorChooser.setBounds(20, 20, 128, 152);
        colorChooser.setBackground(Const.COLOR_SETTINGS_TABS_BG);
        JLabel greenL = new JLabel("blau");
        greenL.setUI(new VerticalLabelUI(false));
        greenL.setBounds(2, 20, 20, 128);
        greenL.setHorizontalAlignment(SwingConstants.RIGHT);
        JLabel blueL = new JLabel("grün");
        blueL.setBounds(20, 2, 128, 20);
        JLabel redL = new JLabel("rot");
        redL.setBounds(2, 144, 20, 20);
        
        JPanel colorChooserBorder1 = new JPanel(null);
        JPanel colorChooserBorder2 = new JPanel(null);
        colorChooserBorder1.setBounds(19, 19, 131, 131);
        colorChooserBorder1.setBorder(new EtchedBorder());
        colorChooserBorder2.setBounds(19, 150, 131, 12);
        colorChooserBorder2.setBorder(new EtchedBorder());

        pColorChooser.add(greenL);
        pColorChooser.add(blueL);
        pColorChooser.add(redL);
        pColorChooser.add(colorChooser);
        pColorChooser.add(colorChooserBorder2);
        pColorChooser.add(colorChooserBorder1);
        pColorChooser.setBackground(Const.COLOR_SETTINGS_TABS_BG);

        colorRadioButton = new JRadioButton[ColorSet.MAXCOLORS];
        ButtonGroup gruppe = new ButtonGroup();
        colorRects = new JLabel[ColorSet.MAXCOLORS];
        for (byte i = 0x00; i < ColorSet.MAXCOLORS; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            colorRadioButton[i] = new JRadioButton(ColorSet.NAMES[i]);
            colorRadioButton[i].addItemListener(this);
            gruppe.add(colorRadioButton[i]);
            colorRects[i] = new JLabel("lalabla");
            colorRects[i].setOpaque(true);
            colorRects[i].setBackground(caller.getConfig().getColors()[i]);
            colorRects[i].setForeground(caller.getConfig().getColors()[i]);
            colorRects[i].setBorder(new EtchedBorder());
            row.add(colorRadioButton[i]);
            row.add(colorRects[i]);
            pRadiosAll.add(row);
        }
        colorRadioButton[0].setSelected(true);
//        colorChooser.setColor(caller.getConfig().getColors()[0]);
//        pRadiosAll.setBorder(new EtchedBorder());

        colorField = new JTextField[3];
        colorField[0] = new JTextField(3);
        colorField[1] = new JTextField(3);
        colorField[2] = new JTextField(3);
        colorField[0].setText("" + colorRects[0].getBackground().getRed());
        colorField[1].setText("" + colorRects[0].getBackground().getGreen());
        colorField[2].setText("" + colorRects[0].getBackground().getBlue());
        colorField[0].addKeyListener(this);
        colorField[1].addKeyListener(this);
        colorField[2].addKeyListener(this);
        pColorTexts.add(new JLabel("R="));
        pColorTexts.add(colorField[0]);
        pColorTexts.add(new JLabel("G="));
        pColorTexts.add(colorField[1]);
        pColorTexts.add(new JLabel("B="));
        pColorTexts.add(colorField[2]);
        pColorTexts.setBackground(Const.COLOR_SETTINGS_TABS_BG);

        pColors.add(pColorChooser, BorderLayout.CENTER);
        pColors.add(pColorTexts, BorderLayout.SOUTH);
        
        JScrollPane jsp = new JScrollPane(pRadiosAll);
        
        pMain.add(pColors, BorderLayout.WEST);
        pMain.add(jsp, BorderLayout.CENTER);
        
        pC.add(pMain);
        pC.setBorder(new EmptyBorder(5, 5, 5, 5));
        pC.setBackground(Const.COLOR_SETTINGS_TABS_BG);
        
        defaultButtonTab3 = new JButton(defaultButtonText);
        defaultButtonTab3.addActionListener(this);
        pS.add(defaultButtonTab3);
        pS.setBackground(Const.COLOR_SETTINGS_TABS_BG);
        
        pAll.add(pC, BorderLayout.CENTER);
        pAll.add(pS, BorderLayout.SOUTH);
        return pAll;
    }

    /**
     * Arrange tag 4: "Import/Export"
     */
    private JPanel arrangeTab4() {
        JPanel pAll = new JPanel(new BorderLayout());
        JPanel pImEx = new JPanel(new GridLayout(3, 1));
        JPanel pWest = new JPanel(new BorderLayout());
        JPanel pMain = new JPanel(new BorderLayout());

        ButtonGroup g = new ButtonGroup();
        importButton = new JRadioButton("Import");
        exportButton = new JRadioButton("Export");
        g.add(importButton);
        g.add(exportButton);
        importButton.addItemListener(this);
        exportButton.addItemListener(this);
        importButton.setBackground(Const.COLOR_SETTINGS_TABS_BG);
        exportButton.setBackground(Const.COLOR_SETTINGS_TABS_BG);
        exportButton.setSelected(true);
        fileButton = new JButton("Datei...");
        importExportLabel = new JLabel("<html><body>Rechts die<br>Auswahl treffen<br>und dann \"Datei...\"<br>klicken zum<br>Exportieren.</body></html>");
        fileButton.addActionListener(this);

        pImEx.add(importButton);
        pImEx.add(exportButton);
        pImEx.add(fileButton);
        pImEx.setBackground(Const.COLOR_SETTINGS_TABS_BG);
        pWest.add(pImEx, BorderLayout.NORTH);
        pWest.add(importExportLabel, BorderLayout.SOUTH);
        pWest.setBackground(Const.COLOR_SETTINGS_TABS_BG);

        /* Get all events but without holidays */
        Vector<Event> events = caller.getAllEvents();
        Vector<Event> withoutHolidays = new Vector<Event>();
        for (Event e : events)
            if (!e.isHoliday() && !e.isSpecial())
                withoutHolidays.add(e);

        exportPanel = fillTable(withoutHolidays, caller.getConfig());
        scrollPane = new JScrollPane(exportPanel);

        titleLabel = new JLabel("Eigene Einstellungen und Ereignisse:");
        titleLabel.setFont(Const.FONT_BORDER_TEXT);
        pMain.add(titleLabel, BorderLayout.NORTH);
        pMain.add(scrollPane, BorderLayout.CENTER);
        pMain.setBackground(Const.COLOR_SETTINGS_TABS_BG);
        pAll.add(pWest, BorderLayout.WEST);
        pAll.add(pMain, BorderLayout.CENTER);
        return pAll;
    }

    /**
     * Arrange tab 5: "Programminfo"
     */
    private JPanel arrangeTab5() {
        JPanel pAll = new JPanel(new BorderLayout());
        JPanel pWest = new JPanel(new BorderLayout());

        updateButton = new JButton("Nach neuer Version suchen");
        updateButton.addActionListener(this);

        JTextArea jt = new JTextArea(
                Const.FILENAME
                +"\nVersion " + Const.VERSION
                +"\nKompiliert am " + Const.LAST_EDIT_DATE + " mit " + Const.COMPILER);
        jt.setBorder(new EtchedBorder());
        jt.setEditable(false);
        jt.setBackground(Const.COLOR_SETTINGS_INFO_BG);
        
        JLabel author = new JLabel(Const.AUTHOR + " (C) 2012");
        author.setAlignmentY(SwingConstants.BOTTOM);
        author.setVerticalAlignment(SwingConstants.BOTTOM);
        
        pAll.add(jt, BorderLayout.NORTH);
        pWest.add(updateButton, BorderLayout.NORTH);
        pWest.add(author, BorderLayout.CENTER);
        pWest.add(new LinkLabel(Const.HOME_URL, Const.HOME_URL), BorderLayout.SOUTH);
        pWest.setBackground(Color.white);
        pWest.setBorder(new EmptyBorder(10, 0, 10, 0));
        pAll.add(pWest, BorderLayout.WEST);
        pAll.add(new JLabel(new ImageIcon(this.getClass().getClassLoader().getResource("media/logo.gif"))));
        pAll.setBorder(new EmptyBorder(5, 5, 5, 5));
        pAll.setBackground(Color.white);
        
        return pAll;
    }

    /**
     * Arranges all elements in this settings frame.
     * Fills the tabs with content.
     * @param tabNo - Tab to select (frame will launch with this
     *         tab shown), see Settings.TAB_XXX
     */
    private void arrangeFrame(int tabNo) {
        setLayout(new BorderLayout());
        tab = new JTabbedPane();
        tab.addTab("Allgemein", arrangeTab1());
        tab.addTab("Feiertage", arrangeTab2());
        tab.addTab("Farben", arrangeTab3());
        tab.addTab("Import/Export", arrangeTab4());
        tab.addTab("Programminfo", arrangeTab5());
        tab.setSelectedIndex(tabNo);

        JPanel ButtonPanel = new JPanel();
        okButton = new JButton("OK");
        cancelButton = new JButton("Abbruch");
        okButton.addActionListener(this);
        cancelButton.addActionListener(this);
        ButtonPanel.add(okButton);
        ButtonPanel.add(cancelButton);

        add(tab, BorderLayout.CENTER);
        add(ButtonPanel, BorderLayout.SOUTH);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setSize(522, 360);
        setLocationRelativeTo(caller.getGUI().getFrame());
        setVisible(true);
        setResizable(true);
    }

    /**
     * Construct a new settings frame.
     * @param c - Parent calendar object
     */
    public Settings(Calendar c) {
        this(c, 0);
    }

    /**
     * Construct a new settings frame.
     * @param c - Parent calendar object
     * @param tabNo - Tab to select (frame will launch with this
     *         tab shown), see Settings.TAB_XXX
     */
    public Settings(Calendar c, int tabNo) {
        super(c.getGUI().getFrame(), "Einstellungen...");
        caller = c;
        setIcon();
        arrangeFrame(tabNo);
    }

    /**
     * For tab "Import/Export": Construct a JPanel with the possible 
     * positions for importing or exporting. This JPanel will be 
     * within the JScrollPanel.
     * @param e - List of events to import/export
     * @param c - Configuration to import/export
     * @return JPanel that contains all events and configurations
     *         (which differ from the default) as selectable check boxes.
     */
    private JPanel fillTable(Vector<Event> e, Configuration c) {
        possibleEvents = e;
        possibleConfig = c;

        JPanel pScroll = new JPanel();
        JPanel pScrollConfig = new JPanel();
        JPanel pScrollEvents = new JPanel();

        /* Get config and event lines to print */
        Vector<String> configLines = new Vector<String>();
        Vector<String> eventLines = new Vector<String>();

        if (!c.equals(Configuration.defaultConfig)) {
            if (c.getView() != Configuration.defaultConfig.getView())
                configLines.add("Standardansicht: " + Configuration.VIEW_LABELS[c.getView()]);
            if (c.getReminder() != Configuration.defaultConfig.getReminder())
                configLines.add(Event.getReminderAsString(c.getReminder(), false));
            if (c.getOnCloseAction() != Configuration.defaultConfig.getOnCloseAction())
                configLines.add("Beim Schließen: " + Configuration.ON_CLOSE_LABELS[c.getOnCloseAction()]);
            if (c.getOnClickDayAction() != Configuration.defaultConfig.getOnClickDayAction())
                configLines.add("Aktion beim Klick auf Tage: " + Configuration.ON_CLICK_DAY_LABELS[c.getOnClickDayAction()]);
            if (c.getOnClickEventAction() != Configuration.defaultConfig.getOnClickEventAction())
                configLines.add("Aktion beim Klick auf Ereignisse: " + Configuration.ON_CLICK_EVENT_LABELS[c.getOnClickEventAction()]);
            if (c.getStyle() != Configuration.defaultConfig.getStyle())
                configLines.add("Style: " + Configuration.STYLE_LABELS[c.getStyle()]);
            if (c.getAutoUpdate() != Configuration.defaultConfig.getAutoUpdate())
                configLines.add("Automtisch nach Updates suchen: " + 
                        (c.getAutoUpdate() ? "Ja" : "Nein"));
            if (c.getMoon() != Configuration.defaultConfig.getMoon())
                configLines.add("Mondphasen anzeigen: " + 
                        (c.getMoon() ? "Ja" : "Nein"));
            if (c.getButtonTexts() != Configuration.defaultConfig.getButtonTexts())
                configLines.add("Button-Texte anzeigen: " + 
                        (c.getButtonTexts() ? "Ja" : "Nein"));
            if (c.getSystrayStart() != Configuration.defaultConfig.getSystrayStart())
                configLines.add("Im Systray starten: " + 
                        (c.getSystrayStart() ? "Ja" : "Nein"));
            if (c.getPlayTheme() != Configuration.defaultConfig.getPlayTheme())
                configLines.add("Erinnerungsmelodie abspielen: " + 
                        (c.getPlayTheme() ? "Ja" : "Nein"));
            if (c.getTheme() != Configuration.defaultConfig.getTheme())
                configLines.add("Eigene Erinnerungsmelodie (nur Dateipfad)");
            if (c.getHolidays() != Configuration.defaultConfig.getHolidays())
                configLines.add("Feiertage");
            if (c.getSpecialDays() != Configuration.defaultConfig.getSpecialDays())
                configLines.add("Besondere Tage");
            for (byte i = 0x00; i < ColorSet.MAXCOLORS; i++)
                if (!c.getColors()[i].equals(ColorSet.DEFAULT[i]))
                    configLines.add("Farbe (" + ColorSet.NAMES[i] + ")");
        }

        for (Event ev : e)
            eventLines.add(ev.getDate().dateToString(true)
                    + (ev.getDate().hasTime() ? ", " + ev.getDate().timeToString() + " Uhr " : " ")
                    + ev.getName() + " " + Frequency.getLabel(ev.getFrequency(), ev.getDate()));

        /* Calculate number of total lines */
        numConfig = configLines.size();
        numEvents = eventLines.size();
        int index = 0;
        int emptyLines = 7;
        importExportBoxes = new JCheckBox[numConfig + numEvents];
        allImportExportBoxes = new JCheckBox[2];

        emptyLines -= numConfig > 0 ? (numConfig + 1) : 0;
        emptyLines -= numEvents > 0 ? (numEvents + 1) : 0;
        if (emptyLines < 0)
            emptyLines = 0;
        logger.debug("fill with empty lines: " + emptyLines);
        pScrollConfig.setLayout(new GridLayout(numConfig > 0 ? numConfig + 1 : 0, 1));
        pScrollEvents.setLayout(new GridLayout((numEvents > 0 ? numEvents + 1 : 0)
                + emptyLines, 1));
        pScroll.setLayout(new BorderLayout());

        /* fill */
        if (configLines.size() > 0) {
            allImportExportBoxes[0] = new JCheckBox("Alle Einstellungen...");
            allImportExportBoxes[0].addItemListener(this);
            pScrollConfig.add(allImportExportBoxes[0]);
        }
        for (String s : configLines) {
            importExportBoxes[index] = new JCheckBox(s);
            pScrollConfig.add(importExportBoxes[index++]);
        }
        if (eventLines.size() > 0) {
            allImportExportBoxes[1] = new JCheckBox("Alle Ereignisse...");
            allImportExportBoxes[1].addItemListener(this);
            pScrollEvents.add(allImportExportBoxes[1]);
        }
        for (String s : eventLines) {
            importExportBoxes[index] = new JCheckBox(s);
            pScrollEvents.add(importExportBoxes[index++]);
        }

        if (numConfig + numEvents == 0)
            pScrollEvents.add(new JLabel("Keine Daten zum Portieren vorhanden..."));

        pScrollConfig.setBorder(new EtchedBorder());
        pScrollEvents.setBorder(new EtchedBorder());
        if (numConfig > 0)
            pScroll.add(pScrollConfig, BorderLayout.NORTH);
        pScroll.add(pScrollEvents, BorderLayout.CENTER);
        return pScroll;
    }

    /**
     * For tab "Colors": Returns the index of the selected check box.
     * @return Selected index.
     */
    private int getSelectedColorIndex() {
        for (int i = 0; i < ColorSet.MAXCOLORS; i++)
            if (colorRadioButton[i].isSelected())
                return i;
        return -1;
    }

    /**
     * The color in the colorChooser has been changed. This will
     * update the colorRect and the R,G,B-textFields properly. 
     * @param c - Color that has been chosen
     */
    public void colorChosen(Color c) {
        colorRects[getSelectedColorIndex()].setForeground(c);
        colorRects[getSelectedColorIndex()].setBackground(c);
        colorField[0].setText("" + c.getRed());
        colorField[1].setText("" + c.getGreen());
        colorField[2].setText("" + c.getBlue());
    }

    /**
     * Import events and configurations that has been parsed
     * from a external XML file.
     * Properties stored in possibleConfig and possibleEvents.
     * This will call the parent calendar object to apply the
     * new configuration and add new events.
     */
    private void doImport() {
        byte view = caller.getConfig().getView();
        byte reminder = caller.getConfig().getReminder();
        byte atClose = caller.getConfig().getOnCloseAction();
        byte atClickDay = caller.getConfig().getOnClickDayAction();
        byte atClickEvent = caller.getConfig().getOnClickEventAction();
        byte style = caller.getConfig().getStyle();
        int holidayID = caller.getConfig().getHolidays();
        int specialID = caller.getConfig().getSpecialDays();
        boolean autoUpdate = caller.getConfig().getAutoUpdate();
        boolean moon = caller.getConfig().getMoon();
        String theme = caller.getConfig().getTheme();
        boolean systrayStart = caller.getConfig().getSystrayStart();
        boolean playTheme = caller.getConfig().getPlayTheme();
        boolean buttonTexts = caller.getConfig().getButtonTexts();
        Color[] colors = caller.getConfig().getColors();

        /*
         * Check which config lines differ from the default
         * configuration
         */
        int index = 0;
        if (possibleConfig != Configuration.defaultConfig) {
            if (possibleConfig.getView() != Configuration.defaultConfig.getView())
                if (importExportBoxes[index++].isSelected()) {
                    view = possibleConfig.getView();
                    logger.debug("import view");
                }
            if (possibleConfig.getReminder() != Configuration.defaultConfig.getReminder())
                if (importExportBoxes[index++].isSelected()) {
                    reminder = possibleConfig.getReminder();
                    logger.debug("import reminder");
                }
            if (possibleConfig.getOnCloseAction() != Configuration.defaultConfig.getOnCloseAction())
                if (importExportBoxes[index++].isSelected()) {
                    atClose = possibleConfig.getOnCloseAction();
                    logger.debug("import atClose");
                }
            if (possibleConfig.getOnClickDayAction() != Configuration.defaultConfig.getOnClickDayAction())
                if (importExportBoxes[index++].isSelected()) {
                    atClickDay = possibleConfig.getOnClickDayAction();
                    logger.debug("import atClickDay");
                }
            if (possibleConfig.getOnClickEventAction() != Configuration.defaultConfig.getOnClickEventAction())
                if (importExportBoxes[index++].isSelected()) {
                    atClickEvent = possibleConfig.getOnClickEventAction();
                    logger.debug("import atClickEvent");
                }
            if (possibleConfig.getStyle() != Configuration.defaultConfig.getStyle())
                if (importExportBoxes[index++].isSelected()) {
                    style = possibleConfig.getStyle();
                    logger.debug("import style");
                }
            if (possibleConfig.getAutoUpdate() != Configuration.defaultConfig.getAutoUpdate())
                if (importExportBoxes[index++].isSelected()) {
                    autoUpdate = possibleConfig.getAutoUpdate();
                    logger.debug("import auto update");
                }
            if (possibleConfig.getMoon() != Configuration.defaultConfig.getMoon())
                if (importExportBoxes[index++].isSelected()) {
                    moon = possibleConfig.getMoon();
                    logger.debug("import moon");
                }
            if (possibleConfig.getButtonTexts() != Configuration.defaultConfig.getButtonTexts())
                if (importExportBoxes[index++].isSelected()) {
                    buttonTexts = possibleConfig.getButtonTexts();
                    logger.debug("import buttonTexts");
                }
            if (possibleConfig.getSystrayStart() != Configuration.defaultConfig.getSystrayStart())
                if (importExportBoxes[index++].isSelected()) {
                    systrayStart = possibleConfig.getSystrayStart();
                    logger.debug("import systrayStart");
                }
            if (possibleConfig.getPlayTheme() != Configuration.defaultConfig.getPlayTheme())
                if (importExportBoxes[index++].isSelected()) {
                    playTheme = possibleConfig.getPlayTheme();
                    logger.debug("import playTheme");
                }
            if (possibleConfig.getTheme() != Configuration.defaultConfig.getTheme())
                if (importExportBoxes[index++].isSelected()) {
                    theme = possibleConfig.getTheme();
                    logger.debug("import theme");
                }
            if (possibleConfig.getHolidays() != Configuration.defaultConfig.getHolidays())
                if (importExportBoxes[index++].isSelected()) {
                    holidayID = possibleConfig.getHolidays();
                    logger.debug("import holidays");
                }
            if (possibleConfig.getSpecialDays() != Configuration.defaultConfig.getSpecialDays())
                if (importExportBoxes[index++].isSelected()) {
                    specialID = possibleConfig.getSpecialDays();
                    logger.debug("import special days");
                }
            for (byte i = 0x00; i < ColorSet.MAXCOLORS; i++)
                if (!possibleConfig.getColors()[i].equals(ColorSet.DEFAULT[i]))
                    if (importExportBoxes[index++].isSelected()) {
                        colors[i] = possibleConfig.getColors()[i];
                        logger.debug("import color " + ColorSet.NAMES[i]);
                    }

            caller.setConfig(new Configuration(view, reminder, atClose,
                    atClickDay, atClickEvent, style, colors, holidayID, 
                    specialID, autoUpdate, moon, theme, systrayStart, 
                    playTheme, buttonTexts));
        }

        logger.debug("import index=" + index);

        /*
         * Add new events
         */
        for (int i = index; i < possibleEvents.size() + index; i++)
            if (importExportBoxes[i].isSelected())
                caller.newEvent(possibleEvents.elementAt(i - index));
    }

    /**
     * Opens an open-file-dialog for choosing a XML file to import.
     * Parses the XML file and stores events and configuration
     * in possibleEvents and possibleConfig.
     */
    private void openImport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("XML-Datei", "xml"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String filename = chooser.getSelectedFile().getPath();
            Vector<Event> events = new Vector<Event>();
            Configuration config = Configuration.defaultConfig;
            logger.debug("import file: " + filename);

            /* new version file */
            if (filename.endsWith(".xml")) {
                XMLParser parser = new XMLParser();
                try {
                    parser.parse(filename);
                } catch (CannotParseException e) {
                    JOptionPane.showMessageDialog(this,
                            "Der Inhalt der Datei \"" + filename
                            + "\" passt nicht ins Schema.\n> " + e.getMessage() + "\nDer Import kann nicht durchgeführt werden.",
                            "Fehler beim Lesen...", JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (FileNotFoundException e) {
                    JOptionPane.showMessageDialog(this, "Die Datei \""
                            + filename + "\" kann nicht gefunden werden.",
                            "Fehler beim Lesen...", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                config = parser.getConfig();
                events = parser.getEvents();
            }

            titleLabel.setText(filename);
            titleLabel.setToolTipText(filename);
            scrollPane.setViewportView(fillTable(events, config));
            importExportLabel.setText("<html><body>Nun rechts<br>die Auswahl<br>zum Importieren<br>treffen und dann<br>\"OK\" klicken.</body></html>");
        }
    }

    /**
     * Opens a save-file-dialog for writing all checked events
     * and configurations.
     */
    private void doExport() {
        boolean somethingSelected = false;
        for (JCheckBox c : importExportBoxes)
            if (c.isSelected()) {
                somethingSelected = true;
                break;
            }
        if (!somethingSelected) {
            JOptionPane.showMessageDialog(this,
                    "Es sind keine Daten zum Exportieren ausgewählt.",
                    "Exportieren...", JOptionPane.PLAIN_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("XML-Datei", "xml"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            byte view = Configuration.defaultConfig.getView();
            byte reminder = Configuration.defaultConfig.getReminder();
            byte atClose = Configuration.defaultConfig.getOnCloseAction();
            byte atClickDay = Configuration.defaultConfig.getOnClickDayAction();
            byte atClickEvent = Configuration.defaultConfig.getOnClickEventAction();
            byte style = Configuration.defaultConfig.getStyle();
            int holidayID = Configuration.defaultConfig.getHolidays();
            int specialID = Configuration.defaultConfig.getSpecialDays();
            boolean autoUpdate = Configuration.defaultConfig.getAutoUpdate();
            boolean moon = Configuration.defaultConfig.getMoon();
            String theme = Configuration.defaultConfig.getTheme();
            boolean systrayStart = Configuration.defaultConfig.getSystrayStart();
            boolean playTheme = Configuration.defaultConfig.getPlayTheme();
            boolean buttonTexts = Configuration.defaultConfig.getButtonTexts();
            Color[] colors = Configuration.defaultConfig.getColors();

            /*
             * Check configurations which differ from the default
             */
            int index = 0;
            if (!possibleConfig.equals(Configuration.defaultConfig)) {
                if (possibleConfig.getView() != Configuration.defaultConfig.getView())
                    if (importExportBoxes[index++].isSelected()) {
                        view = possibleConfig.getView();
                        logger.debug("export view");
                    }
                if (possibleConfig.getReminder() != Configuration.defaultConfig.getReminder())
                    if (importExportBoxes[index++].isSelected()) {
                        reminder = possibleConfig.getReminder();
                        logger.debug("export reminder");
                    }
                if (possibleConfig.getOnCloseAction() != Configuration.defaultConfig.getOnCloseAction())
                    if (importExportBoxes[index++].isSelected()) {
                        atClose = possibleConfig.getOnCloseAction();
                        logger.debug("export atClose");
                    }
                if (possibleConfig.getOnClickDayAction() != Configuration.defaultConfig.getOnClickDayAction())
                    if (importExportBoxes[index++].isSelected()) {
                        atClickDay = possibleConfig.getOnClickDayAction();
                        logger.debug("export atClickDay");
                    }
                if (possibleConfig.getOnClickEventAction() != Configuration.defaultConfig.getOnClickEventAction())
                    if (importExportBoxes[index++].isSelected()) {
                        atClickEvent = possibleConfig.getOnClickEventAction();
                        logger.debug("export atClickEvent");
                    }
                if (possibleConfig.getStyle() != Configuration.defaultConfig.getStyle())
                    if (importExportBoxes[index++].isSelected()) {
                        style = possibleConfig.getStyle();
                        logger.debug("export style");
                    }
                if (possibleConfig.getAutoUpdate() != Configuration.defaultConfig.getAutoUpdate())
                    if (importExportBoxes[index++].isSelected()) {
                        autoUpdate = possibleConfig.getAutoUpdate();
                        logger.debug("export auto update");
                    }
                if (possibleConfig.getMoon() != Configuration.defaultConfig.getMoon())
                    if (importExportBoxes[index++].isSelected()) {
                        moon = possibleConfig.getMoon();
                        logger.debug("export moon");
                    }
                if (possibleConfig.getButtonTexts() != Configuration.defaultConfig.getButtonTexts())
                    if (importExportBoxes[index++].isSelected()) {
                        buttonTexts = possibleConfig.getButtonTexts();
                        logger.debug("export buttonTexts");
                    }
                if (possibleConfig.getSystrayStart() != Configuration.defaultConfig.getSystrayStart())
                    if (importExportBoxes[index++].isSelected()) {
                        systrayStart = possibleConfig.getSystrayStart();
                        logger.debug("export systrayStart");
                    }
                if (possibleConfig.getPlayTheme() != Configuration.defaultConfig.getPlayTheme())
                    if (importExportBoxes[index++].isSelected()) {
                        playTheme = possibleConfig.getPlayTheme();
                        logger.debug("export playTheme");
                    }
                if (possibleConfig.getTheme() != Configuration.defaultConfig.getTheme())
                    if (importExportBoxes[index++].isSelected()) {
                        theme = possibleConfig.getTheme();
                        logger.debug("export theme");
                    }
                if (possibleConfig.getHolidays() != Configuration.defaultConfig
                        .getHolidays())
                    if (importExportBoxes[index++].isSelected()) {
                        holidayID = possibleConfig.getHolidays();
                        logger.debug("export holidays");
                    }
                if (possibleConfig.getSpecialDays() != Configuration.defaultConfig
                        .getSpecialDays())
                    if (importExportBoxes[index++].isSelected()) {
                        specialID = possibleConfig.getSpecialDays();
                        logger.debug("export special days");
                    }
                for (byte i = 0x00; i < ColorSet.MAXCOLORS; i++)
                    if (!possibleConfig.getColors()[i].equals(ColorSet.DEFAULT[i]))
                        if (importExportBoxes[index++].isSelected()) {
                            colors[i] = possibleConfig.getColors()[i];
                            logger.debug("export color " + ColorSet.NAMES[i]);
                        }
            }

            /*
             * Build configuration to write
             */
            Configuration cfg = new Configuration(view, reminder, atClose,
                    atClickDay, atClickEvent, style, colors, holidayID, 
                    specialID, autoUpdate, moon, theme, systrayStart, 
                    playTheme, buttonTexts);

            logger.debug("export index=" + index);

            /*
             * Get selected events
             */
            Vector<Event> selectedEvents = new Vector<Event>();
            for (int i = index; i < possibleEvents.size() + index; i++)
                if (importExportBoxes[i].isSelected())
                    selectedEvents.add(possibleEvents.elementAt(i - index));

            /*
             * Write checked configuration and checked events
             * into chosen file
             */
            String filename = chooser.getSelectedFile().getPath();
            logger.debug("export file=" + filename);
            if (!filename.endsWith(".xml"))
                filename = filename.concat(".xml");
            caller.save(selectedEvents, cfg, filename);

            JOptionPane.showMessageDialog(this,
                    "Die gewählten Daten wurden in die Datei\n" + filename
                    + "\ngespeichert.\n\nHinweis: Anhänge und Notizen von Ereignissen\nkönnen nicht exportiert werden.", "Export erfolgreich...",
                    JOptionPane.PLAIN_MESSAGE);
        }
    }
    
    /**
     * 
     * @return Selected Look&Feel. See Configuration.STYLE_XXX.
     */
    public byte getSelectedLookAndFeel() {
        String styleS = (String) styleBox.getSelectedItem();
        for (byte i = Configuration.STYLE_SYSTEM; i <= Configuration.STYLE_NIMBUS; i++)
            if (styleS.equals(Configuration.STYLE_LABELS[i])) 
                return i;
        return Configuration.STYLE_SYSTEM;
    }

    @Override
    public void actionPerformed(ActionEvent a) {
        /*
         * OK button clicked
         */
        if (a.getSource().equals(okButton)) {
            int holidays = 0;
            int specials = 0;
            
            /* encode holidays and special days */
            for (int i = 0; i < holidayBoxes.length; i++)
                if (holidayBoxes[i].isSelected())
                    holidays |= (1 << i);
            for (int i = 0; i < specialDayBoxes.length; i++)
                if (specialDayBoxes[i].isSelected())
                    specials |= (1 << i);

            /* get colors */
            Color[] colors = new Color[ColorSet.MAXCOLORS];
            for (int i = 0; i < ColorSet.MAXCOLORS; i++)
                colors[i] = colorRects[i].getBackground();

            /* apply new configuration */
            caller.setConfig(new Configuration(
                    (byte) caller.getGUI().getFrame().getView(),
                    (byte) remindBox.getSelectedIndex(), 
                    (byte) onCloseBox.getSelectedIndex(), 
                    (byte) onClickDayBox.getSelectedIndex(),
                    (byte) onClickEventBox.getSelectedIndex(),
                    getSelectedLookAndFeel(), colors, holidays, 
                    specials, autoUpdateBox.isSelected(),
                    moonBox.isSelected(), ownThemeBox.isSelected() ?
                            ownThemeLabel.getToolTipText() : null,
                    systrayBox.isSelected(), playThemeBox.isSelected(),
                    buttonTextsBox.isSelected()));

            /* in case of import/export tab */
            if (tab.getSelectedIndex() == TAB_IMEXPORT)
                if (importButton.isSelected())
                    doImport();
            
            /* any case: close this frame */ 
            this.setVisible(false);
            this.dispose();
        } 
        
        /*
         * Use defaults button clicked
         */
        else if (a.getSource().equals(defaultButtonTab1)) {
            logger.debug("TAB GENERAL");
            remindBox.setSelectedIndex(Configuration.defaultConfig.getReminder());
            onCloseBox.setSelectedIndex(Configuration.defaultConfig.getOnCloseAction());
            styleBox.setSelectedIndex(Configuration.defaultConfig.getStyle());
            autoUpdateBox.setSelected(Configuration.defaultConfig.getAutoUpdate());
            moonBox.setSelected(Configuration.defaultConfig.getMoon());
            ownThemeBox.setSelected(false);
        }
        else if (a.getSource().equals(defaultButtonTab2)) {
            logger.debug("TAB HOLIDAYS");
            for (int i = 0; i < Holidays.TOTAL_HOLIDAY_LAW; i++)
                holidayBoxes[i].setSelected((Holidays.DEFAULT_HOLIDAYS & (1 << i)) == (1 << i));
            for (int i = 0; i < Holidays.TOTAL_HOLIDAY_SPECIAL; i++)
                specialDayBoxes[i].setSelected((Holidays.DEFAULT_SPECIAL & (1 << i)) == (1 << i));

        } 
        else if (a.getSource().equals(defaultButtonTab3)) {
            logger.debug("TAB COLORS");
            for (byte i = 0x00; i < ColorSet.MAXCOLORS; i++) {
                colorRects[i].setForeground(ColorSet.DEFAULT[i]);
                colorRects[i].setBackground(ColorSet.DEFAULT[i]);
            }
            colorField[0].setText("" + colorRects[getSelectedColorIndex()].getBackground().getRed());
            colorField[1].setText("" + colorRects[getSelectedColorIndex()].getBackground().getGreen());
            colorField[2].setText("" + colorRects[getSelectedColorIndex()].getBackground().getBlue());
        }
        
        /*
         * File-chooser button clicked
         */
        else if (a.getSource().equals(fileButton)) {
            if (importButton.isSelected())
                openImport();
            else if (exportButton.isSelected())
                doExport();
        } 
        
        /*
         * Search update button clicked
         */
        else if (a.getSource().equals(updateButton))
            new Update(caller.getGUI().getFrame(), false);
        
        /*
         * Play theme button clicked
         */
        else if (a.getSource().equals(playThemeButton)) {
            try {
                if (ownThemeBox.isSelected())
                    Music.playTheme(ownThemeLabel.getToolTipText(), false);
                else
                    Music.playTheme(Const.DEFAULT_THEME, true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(caller.getGUI().getFrame(), "Die Datei kann nicht abgespielt werden.\nFehler: "+ex.toString(), "Fehler beim Abspielen", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
        
        /*
         * Choose theme file button clicked 
         */
        else if (a.getSource().equals(chooseButton)) {
            String dir = ownThemeLabel.getToolTipText().substring(0,
                    ownThemeLabel.getToolTipText().lastIndexOf(File.separator));
            logger.debug("theme dir=" + dir);
            JFileChooser chooser = new JFileChooser(dir);
            chooser.setFileFilter(new FileNameExtensionFilter("Musik-Datei (*.wav)", "wav"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();
                ownThemeLabel.setText(path);
                ownThemeLabel.setToolTipText(path);
            }
        }
        
        /*
         * Cancel
         */
        else {
            if (currentUI != initUI)
                caller.getGUI().getFrame().setUI(caller.getConfig().getStyle(), true);
            this.setVisible(false);
            this.dispose();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent i) {
        if (tab.getSelectedIndex() == TAB_COLORS) {
            int selectedColor = getSelectedColorIndex();
            if (selectedColor == -1)
                return;
            Color color = colorRects[selectedColor].getBackground();
            colorField[0].setText("" + color.getRed());
            colorField[1].setText("" + color.getGreen());
            colorField[2].setText("" + color.getBlue());
            colorChooser.setColor(null, color);
        }
        else if (tab.getSelectedIndex() == TAB_IMEXPORT) {
            if (i.getSource().equals(importButton)) {
                scrollPane.setViewportView(new JPanel());
                importExportLabel.setText("<html><body>\"Datei...\" klicken,<br>um Datei zum<br>Importieren zu<br>wählen.</body></html>");
                titleLabel.setText("Noch keine Datei gewählt...");
            } else if (i.getSource().equals(exportButton)) {
                scrollPane.setViewportView(exportPanel);
                importExportLabel.setText("<html><body>Rechts die<br>Auswahl treffen<br>und dann \"Datei...\"<br>klicken zum<br>Exportieren.</body></html>");
                titleLabel.setText("Eigene Einstellungen und Ereignisse:");
            } else if (allImportExportBoxes[0] != null && i.getItem().equals(allImportExportBoxes[0])) {
                for (int a = 0; a < numConfig; a++)
                    importExportBoxes[a].setSelected(allImportExportBoxes[0].isSelected());
            } else if (allImportExportBoxes[1] != null && i.getItem().equals(allImportExportBoxes[1])) {
                for (int a = numConfig; a < numEvents + numConfig; a++)
                    importExportBoxes[a].setSelected(allImportExportBoxes[1].isSelected());
            }
        }
        else if (i.getSource().equals(styleBox)) {
            currentUI = getSelectedLookAndFeel();
            caller.getGUI().getFrame().setUI(currentUI, true);
            SwingUtilities.updateComponentTreeUI(this);
        }
        else if (i.getSource().equals(playThemeBox)) {
            ownThemeBox.setEnabled(playThemeBox.isSelected());
            chooseButton.setEnabled(playThemeBox.isSelected());
            playThemeButton.setEnabled(playThemeBox.isSelected());
        }
        /*
         * Choose own theme file
         */
        else if (i.getItem().equals(ownThemeBox)) {
            if (ownThemeBox.isSelected()) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileNameExtensionFilter("Musik-Datei (*.wav)", "wav"));
                if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    String path = chooser.getSelectedFile().getAbsolutePath();
                    ownThemeLabel.setText(path);
                    ownThemeLabel.setToolTipText(path);
                    chooseButton.setEnabled(true);
                    
                    /* ??? */
                    ownThemeBox.removeItemListener(this);
                    ownThemeBox.setSelected(true);
                    ownThemeBox.addItemListener(this);
                }
            }
            else {
                ownThemeLabel.setText("");
                ownThemeLabel.setToolTipText("");
                chooseButton.setEnabled(false);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent k) {
    }

    @Override
    public void keyReleased(KeyEvent k) {
        int r = 0, g = 0, b = 0;
        try {
            r = Integer.parseInt(colorField[0].getText());
            g = Integer.parseInt(colorField[1].getText());
            b = Integer.parseInt(colorField[2].getText());
            if (r > 255 || g > 255 || b > 255)
                throw new NumberFormatException();
        } catch (NumberFormatException n) {
            logger.error("[keyReleased] cannot parse inserted number");
        }
        logger.debug("COLOR: R=" + r + " G=" + g + " B=" + b);
        Color color = new Color(r, g, b);
        colorRects[getSelectedColorIndex()].setForeground(color);
        colorRects[getSelectedColorIndex()].setBackground(color);
        colorChooser.setColor(null, color);
    }

    @Override
    public void keyTyped(KeyEvent k) {
    }
}

/**
 * VerticalLabelUI - used to replace the UI on a JLabel to make it vertical
 *
 * @author Created by Jasper Potts (10-Jun-2004)
 * @version 1.0
 */
class VerticalLabelUI 
    extends BasicLabelUI {
    
    static {
        labelUI = new VerticalLabelUI(false);
    }

    protected boolean clockwise;

    VerticalLabelUI(boolean clockwise) {
        super();
        this.clockwise = clockwise;
    }

    public Dimension getPreferredSize(JComponent c) {
        Dimension dim = super.getPreferredSize(c);
        return new Dimension(dim.height, dim.width);
    }

    private static Rectangle paintIconR = new Rectangle();
    private static Rectangle paintTextR = new Rectangle();
    private static Rectangle paintViewR = new Rectangle();
    private static Insets paintViewInsets = new Insets(0, 0, 0, 0);

    public void paint(Graphics g, JComponent c) {
        JLabel label = (JLabel) c;
        String text = label.getText();
        Icon icon = (label.isEnabled()) ? label.getIcon() : label
                .getDisabledIcon();

        if ((icon == null) && (text == null))
            return;

        FontMetrics fm = g.getFontMetrics();
        paintViewInsets = c.getInsets(paintViewInsets);

        paintViewR.x = paintViewInsets.left;
        paintViewR.y = paintViewInsets.top;

        // Use inverted height & width
        paintViewR.height = c.getWidth()
                - (paintViewInsets.left + paintViewInsets.right);
        paintViewR.width = c.getHeight()
                - (paintViewInsets.top + paintViewInsets.bottom);

        paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
        paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;

        String clippedText = layoutCL(label, fm, text, icon, paintViewR,
                paintIconR, paintTextR);

        Graphics2D g2 = (Graphics2D) g;
        AffineTransform tr = g2.getTransform();
        if (clockwise) {
            g2.rotate(Math.PI / 2);
            g2.translate(0, -c.getWidth());
        } else {
            g2.rotate(-Math.PI / 2);
            g2.translate(-c.getHeight(), 0);
        }

        if (icon != null)
            icon.paintIcon(c, g, paintIconR.x, paintIconR.y);

        if (text != null) {
            int textX = paintTextR.x;
            int textY = paintTextR.y + fm.getAscent();

            if (label.isEnabled())
                paintEnabledText(label, g, clippedText, textX, textY);
            else
                paintDisabledText(label, g, clippedText, textX, textY);
        }

        g2.setTransform(tr);
    }
}

