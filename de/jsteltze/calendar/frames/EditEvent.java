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
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;

import de.jsteltze.calendar.Calendar;
import de.jsteltze.calendar.Event;
import de.jsteltze.calendar.Frequency;
import de.jsteltze.calendar.config.Const;
import de.jsteltze.calendar.exceptions.InvalidDateException;
import de.jsteltze.common.Copy;
import de.jsteltze.common.ImageButton;
import de.jsteltze.common.ImageButtonListener;
import de.jsteltze.common.LinkLabel;
import de.jsteltze.common.calendar.Date;

/**
 * Frame for editing / creating events.
 * @author Johannes Steltzer
 *
 */
public class EditEvent 
    extends JDialog 
    implements ActionListener, KeyListener, ItemListener, 
        WindowListener, MouseListener, ImageButtonListener {
    
    private static final long serialVersionUID = 1L;
    
    /** maximum length for event name */
    private static final int MAX_LEN_NAME = 30;
    
    /** text for extending (expert mode) */
    private static final String EXTEND = "erweitert ";
    
    /** text for reducing (easy mode= ) */
    private static final String REDUCE = "einfach ";
    
    /** label for extending / reducing */
    private LinkLabel extendLabel;
    
    /** button for extending / reducing */
    private ImageButton extendButton;
    
    /** name of event (must) */
    private JTextField nameField;
    
    /** attachment file path (optional) */
    private JTextField attachField;
    
    /** additional notes (optional) */
    private JTextArea notesField;
    
    /** starting date (auto-filled) */
    private JTextField dayStartField, monStartField, yearStartField;
    
    /** ending date (auto-filled) */
    private JTextField dayEndField, monEndField, yearEndField;
    
    /** radio  buttons for choosing frequency */
    private JRadioButton freq1, freq2, freq3, freq4;
    
    /** frequency (optional) */
    private JCheckBox mBox, yBox, wBox;
    private JComboBox intervalBox, unitBox;
    private JLabel byWeekdayLabel, byIntervalLabel, byEndOfMonthLabel;
    
    /** "Uhr" */
    private JLabel clockLabel, dotsLabel;
    
    /** event with time (optional) */
    private JCheckBox timeBox;
    
    /** event with attachment (optional) */
    private JCheckBox attachmentBox;
    
    /** event time (optional) */
    private JComboBox hoursBox, minutesBox;

    /** event to edit (null for new event) */
    private Event event;
    
    /** reminder selection */
    private JComboBox remindBox;
    
    /** parent object */
    private Calendar caller;
    
    /** controb buttons */
    private JButton okButton, cancelButton, chooseButton;
    
    /** area to contain extended settings */
    private JPanel extendedSettingsPanel;
    
    /** create a copy of the event */
    private boolean copy = false;
    
    private static Logger logger = Logger.getLogger(EditEvent.class);
    
    /**
     * Arranges all elements in the dialog window.
     */
    private void arrangeDialog() {
        setLayout(new BorderLayout());

        /* General section */
        JPanel generalPanel = new JPanel(new BorderLayout());
        JPanel northPanel = new JPanel(new BorderLayout());
        JPanel generalWest = new JPanel(new GridLayout(2, 1));
        JPanel generalCenter = new JPanel(new GridLayout(2, 1));
        JPanel generalCenterN = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel generalCenterS = new JPanel(new BorderLayout());
        generalCenterS.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        generalWest.add(new JLabel("Datum: "));
        generalWest.add(new JLabel("Name: "));
        
        dayStartField = new JTextField(2);
        monStartField = new JTextField(2);
        yearStartField = new JTextField(4);
        
        nameField = new JTextField(30);
        nameField.addKeyListener(this);
        nameField.setToolTipText("Hier die Kurzbeschreibung für dieses Ereignis eingeben. Dieser Name erscheint in der Kalenderübersicht.");

        generalCenterN.add(dayStartField);
        generalCenterN.add(new JLabel("."));
        generalCenterN.add(monStartField);
        generalCenterN.add(new JLabel("."));
        generalCenterN.add(yearStartField);
        
        /* Fill date text fields */
        if (event != null) {
            dayStartField.setText("" + event.getDate().get(java.util.Calendar.DAY_OF_MONTH));
            monStartField.setText("" + (event.getDate().get(java.util.Calendar.MONTH) + 1));
            yearStartField.setText("" + event.getDate().get(java.util.Calendar.YEAR));
            nameField.setText(event.getName());
            if (event.getEndDate() != null) {
                dayEndField = new JTextField(2);
                monEndField = new JTextField(2);
                yearEndField = new JTextField(4);
                dayEndField.setText("" + event.getEndDate().get(java.util.Calendar.DAY_OF_MONTH));
                monEndField.setText("" + (event.getEndDate().get(java.util.Calendar.MONTH) + 1));
                yearEndField.setText("" + event.getEndDate().get(java.util.Calendar.YEAR));
                generalCenterN.add(new JLabel("bis"));
                generalCenterN.add(dayEndField);
                generalCenterN.add(new JLabel("."));
                generalCenterN.add(monEndField);
                generalCenterN.add(new JLabel("."));
                generalCenterN.add(yearEndField);
            }
        }
        
        generalCenterS.add(nameField, BorderLayout.CENTER);
        generalCenter.add(generalCenterN);
        generalCenter.add(generalCenterS);
        generalPanel.add(generalWest, BorderLayout.WEST);
        generalPanel.add(generalCenter, BorderLayout.CENTER);
        generalPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Allgemein",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                Const.FONT_BORDER_TEXT));
        northPanel.add(generalPanel, BorderLayout.CENTER);
        
        /* Add switch for extending (expert mode) / reducing (easy mode) */
        JPanel extendPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
        extendLabel = new LinkLabel("erweitert ");
        extendLabel.addMouseListener(this);
        extendButton = new ImageButton("media/+.PNG", "media/-.PNG", true);
        extendButton.addButtonListener(this);
        extendPanel.add(extendLabel);
        extendPanel.add(extendButton);
        extendPanel.add(new JLabel("    "));
        northPanel.add(extendPanel, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        
        /* Additional information section */
        extendedSettingsPanel = new JPanel(new BorderLayout());
        JPanel additional = new JPanel(new BorderLayout(5, 5));
        JPanel attachment = new JPanel(new BorderLayout());
        notesField = new JTextArea(5, 30);
        notesField.setFont(nameField.getFont());
        notesField.setToolTipText("Hier ist Platz für Informationen, die nicht in den Namen passen.");
        JScrollPane pScroll = new JScrollPane(notesField,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JLabel notizen = new JLabel("Notizen: ");
        notizen.setAlignmentY(SwingConstants.NORTH);
        notizen.setVerticalAlignment(SwingConstants.NORTH);
        JPanel notesP = new JPanel(new BorderLayout());
        notesP.add(notizen, BorderLayout.WEST);
        notesP.add(pScroll, BorderLayout.CENTER);
        notesP.setBorder(new EmptyBorder(0, 0, 0, 5));
        additional.add(notesP, BorderLayout.NORTH);
        attachmentBox = new JCheckBox("Anhang: ");
        attachmentBox.setToolTipText("Hier kann das Ereignis mit einer Datei auf dem Computer verknüpft werden.");
        attachField = new JTextField();
        attachField.setToolTipText("Hier kann das Ereignis mit einer Datei auf dem Computer verknüpft werden.");
        attachField.setEditable(false);
        chooseButton = new JButton("...");
        chooseButton.setMargin(new Insets(2, 2, 2, 2));
        chooseButton.setEnabled(false);
        chooseButton.addActionListener(this);
        chooseButton.setToolTipText("Datei auswählen");
        if (event != null && event.getID() != -1) {
            notesField.setText(event.getNotes(caller.getWorkspace()));
            File attachmentF = event.getAttachment(caller.getWorkspace());
            if (attachmentF != null) {
                attachField.setText(attachmentF.getPath());
                attachmentBox.setSelected(true);
                chooseButton.setEnabled(true);
            }
        }
        attachmentBox.addItemListener(this);
        attachment.add(attachmentBox, BorderLayout.WEST);
        attachment.add(attachField, BorderLayout.CENTER);
        attachment.add(chooseButton, BorderLayout.EAST);
        attachment.setBorder(new EmptyBorder(5, 5, 5, 5));
        additional.add(attachment, BorderLayout.CENTER);
        
        /* Time section */
        JPanel clock = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timeBox = new JCheckBox("Uhrzeit: ");
        timeBox.addItemListener(this);
        hoursBox = new JComboBox();
        minutesBox = new JComboBox();
        clockLabel = new JLabel("Uhr");
        dotsLabel = new JLabel(" : ");
        for (int i = 0; i < 24; i++) 
            hoursBox.addItem(i);
        for (int i = 0; i < 60; i++) 
            minutesBox.addItem(((i < 10) ? ("0") : ("")) + i);
        
        if (event != null && event.getDate().hasTime()) {
            timeBox.setSelected(true);
            hoursBox.setSelectedIndex(event.getDate().get(java.util.Calendar.HOUR_OF_DAY));
            minutesBox.setSelectedIndex(event.getDate().get(java.util.Calendar.MINUTE));
        }
        else {
            timeBox.setSelected(false);
            hoursBox.setEnabled(false);
            minutesBox.setEnabled(false);
            clockLabel.setEnabled(false);
            dotsLabel.setEnabled(false);
        }
        
        clock.add(timeBox);
        clock.add(hoursBox);
        clock.add(dotsLabel);
        clock.add(minutesBox);
        clock.add(clockLabel);
        additional.add(clock, BorderLayout.SOUTH);
        
        additional.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Zusätzliche Informationen",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                Const.FONT_BORDER_TEXT));
        extendedSettingsPanel.add(additional, BorderLayout.NORTH);

        JPanel settings = new JPanel(new BorderLayout());        
        
        /* Frequency section */
        JPanel frequency = new JPanel(new GridLayout(event.getEndDate() == null ? 4 : 1, 1));
        JPanel freq1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel freq2Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel freq3Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel freq4Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ButtonGroup freqGroup = new ButtonGroup();
        freq1 = new JRadioButton("nach Datum:");
        freq1.setSelected(true);
        freq1.addItemListener(this);
        freqGroup.add(freq1);
        wBox=new JCheckBox("wöchentlich");
        wBox.setToolTipText("Wenn \"monatlich\" und \"jährlich\" nicht aktiviert sind, so gilt das Ereignis nur für diesen Monat.");
        mBox=new JCheckBox("monatlich");
        mBox.setToolTipText("Wenn \"jährlich\" nicht aktiviert ist, so wird dieses Ereignis nur 12 mal in diesem Jahr auftauchen.");
        yBox=new JCheckBox("jährlich");
        yBox.setToolTipText("Beim Einstellen der Regelmäßigkeit auf die richtige Kombination achten!");
        freq1Panel.add(freq1);
        freq1Panel.add(wBox);
        freq1Panel.add(mBox);
        freq1Panel.add(yBox);
        
        frequency.add(freq1Panel);
        freq2 = new JRadioButton("nach Wochentag im Monat:");
        int weekday_cnt = event.getDate().getWeekdayIndex();
        byWeekdayLabel = new JLabel("Jeden " + 
                (weekday_cnt == 0 ? "letzten " : weekday_cnt + ". ") + 
                Date.dayOfWeek2String(event.getDate().get(java.util.Calendar.DAY_OF_WEEK), false) + 
                " im Monat");
        byWeekdayLabel.setEnabled(false);
        freq2.addItemListener(this);
        freqGroup.add(freq2);
        freq2Panel.add(freq2);
        freq2Panel.add(byWeekdayLabel);
        frequency.add(freq2Panel);
        
        freq3 = new JRadioButton("nach Abstand:");
        freq3.addItemListener(this);
        intervalBox = new JComboBox();
        unitBox = new JComboBox();
        for (int i = 1; i <= 30; i++)
            intervalBox.addItem(i);
        intervalBox.setEnabled(false);
        unitBox.addItem("Tage");
        unitBox.addItem("Wochen");
        unitBox.addItem("Monate");
        unitBox.addItem("Jahre");
        unitBox.setEnabled(false);
        byIntervalLabel = new JLabel("Aller");
        byIntervalLabel.setEnabled(false);
        freqGroup.add(freq3);
        freq3Panel.add(freq3);
        freq3Panel.add(byIntervalLabel);
        freq3Panel.add(intervalBox);
        freq3Panel.add(unitBox);
        frequency.add(freq3Panel);
        
        freq4 = new JRadioButton("nach Monatsende:");
        freq4.addItemListener(this);
        int daysToEnd = event.getDate().getDaysToEndOfMonth();
        byEndOfMonthLabel = new JLabel(daysToEnd == 0 ? 
                "Jeden letzten Tag im Monat" : daysToEnd == 1 ?
                "Jeden vorletzten Tag im Monat" :
                daysToEnd + " Tage vor Ende des Monats");
        byEndOfMonthLabel.setEnabled(false);
        freqGroup.add(freq4);
        freq4Panel.add(freq4);
        freq4Panel.add(byEndOfMonthLabel);
        frequency.add(freq4Panel);
        
        if (event != null) {
            if (Frequency.isByDate(event.getFrequency())) {
                freq1.setSelected(true);
                wBox.setSelected(Frequency.isW(event.getFrequency()));
                mBox.setSelected(Frequency.isM(event.getFrequency()));
                yBox.setSelected(Frequency.isY(event.getFrequency()));
            }
            else if (Frequency.isByWeekday(event.getFrequency()))
                freq2.setSelected(true);
            else if (Frequency.isByInterval(event.getFrequency())) {
                freq3.setSelected(true);
                intervalBox.setSelectedIndex(Frequency.getInterval(event.getFrequency()) - 1);
                unitBox.setSelectedIndex(Frequency.getUnit(event.getFrequency()));
            }
            else if (Frequency.isByEndOfMonth(event.getFrequency()))
                freq4.setSelected(true);
        }
        if (event.getEndDate() != null) {
            wBox.setEnabled(false);
            mBox.setEnabled(false);
            yBox.setEnabled(false);
            freq1.setEnabled(false);
        }
        
        frequency.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Regelmäßigkeit",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                Const.FONT_BORDER_TEXT));
        
        
        /* Remind section */
        JPanel reminderArea = new JPanel(new FlowLayout(FlowLayout.LEFT));
        remindBox = new JComboBox();
        for (int i = 0; i < Event.NUMBER_REMINDS; i++)
            remindBox.addItem(Event.getReminderAsString(i, false));
        
        if (event != null)
            remindBox.setSelectedIndex(event.getRemind());
        else
            remindBox.setSelectedIndex(caller.getConfig().getReminder());

        reminderArea.add(remindBox);
        reminderArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Erinnerung",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                Const.FONT_BORDER_TEXT));
        
        if (event.getEndDate() == null)
            settings.add(frequency, BorderLayout.CENTER);
        settings.add(reminderArea, BorderLayout.SOUTH);
        extendedSettingsPanel.add(settings, BorderLayout.CENTER);
        
        JPanel buttons = new JPanel();
        okButton = new JButton("OK");
        cancelButton = new JButton("Abbruch");
        okButton.addActionListener(this);
        cancelButton.addActionListener(this);
        buttons.add(okButton);
        buttons.add(cancelButton);
        
        add(buttons, BorderLayout.SOUTH);
        
        /* somehow on modal dialogs switching focus is tricky */ 
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                nameField.dispatchEvent(new FocusEvent(nameField, FocusEvent.FOCUS_GAINED));
            }
        });
        
        pack();
        setLocationRelativeTo(caller.getGUI().getFrame());
        setVisible(true);
        setResizable(false);
    }
    
    /**
     * Editing (creating) a single event.
     * @param c - Parent calendar object
     * @param ev - Event to edit (to create)
     * @param copy - Create a copy of the event
     */
    public EditEvent(Calendar c, Event ev, boolean copy) {
        /* Set title of dialog */
        super(c.getGUI().getFrame(), true);
        if (ev.getName().length() == 0) {
            if (ev.getFrequency() == Frequency.OCCUR_WEEKLY)
                setTitle("Neues wöchentliches Ereignis...");
            else if (ev.getEndDate() == null)
                setTitle("Neues Ereignis...");
            else
                setTitle("Neues mehrtägiges Ereignis...");
        }
        else
            setTitle("Ereignis bearbeiten...");

        addWindowListener(this);
        this.event = ev;
        this.caller = c;
        this.copy = copy;
        this.caller.setNotisVisible(false);

        arrangeDialog();
    }

    /**
     * Create a new event.
     * @param c - Parent calendar object
     * @param d - Date of the event to create
     */
    public EditEvent(Calendar c, Date d) {
        this(c, new Event(d, "", -1), false);
    }

    /**
     * Create a new multi-day event.
     * @param c - Parent calendar object
     * @param start - Start date
     * @param end - End date
     */
    public EditEvent(Calendar c, Date start, Date end) {
        this(c, new Event(start, end, "", -1), false);
    }

    /**
     * Create a new event on a set of dates which are not 
     * connected to each other (by the use of CTRL key).<br>
     * <b>TODO: not yet implemented!</b>
     * @param c - Parent calendar object
     * @param d - List of dates
     */
    public EditEvent(Calendar c, Vector<Date> d) {
        // TODO
    }

    @Override
    public void actionPerformed(ActionEvent a) {
        if (a.getSource().equals(okButton))
            submit();
        else if (a.getSource().equals(cancelButton))
            windowClosing(null);
        else if (a.getSource().equals(chooseButton))
            attach();
    }

    /**
     * Asks to select a file to attach to an event.
     */
    private void attach() {
        logger.debug("attach");
        JFileChooser jfc;
        if (attachField.getText().equals(""))
            jfc = new JFileChooser();
        else {
            String dir = attachField.getText().substring(0,
                    attachField.getText().lastIndexOf(File.separator));
            logger.debug("attach dir=" + dir);
            jfc = new JFileChooser(dir);
        }
        
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            attachField.setText(jfc.getSelectedFile().getPath());
        else if (attachField.getText().equals(""))
            attachmentBox.setSelected(false);
    }

    /**
     * Pressing button OK. This will add the event (submit changes).
     */
    private void submit() {
        logger.debug("submit");
        /*
         * First check dates in 'Datum'
         */
        int day, mon, yea;
        Date startD = null, endD = null;
        try {
            day = Integer.parseInt(dayStartField.getText());
            mon = Integer.parseInt(monStartField.getText());
            yea = Integer.parseInt(yearStartField.getText());
            startD = new Date(yea, mon - 1, day);
            logger.debug("DATE=" + startD.dateToString(false));
            if (startD.get(java.util.Calendar.YEAR) != yea
                    || startD.get(java.util.Calendar.MONTH) != mon - 1
                    || startD.get(java.util.Calendar.DAY_OF_MONTH) != day)
                throw new InvalidDateException(
                        event.getEndDate() == null ? "Datum" : "Startdatum");

            if (event.getEndDate() != null) {
                if (dayEndField.getText().length() == 0
                        && monEndField.getText().length() == 0
                        && yearEndField.getText().length() == 0)
                    endD = null;
                else {
                    day = Integer.parseInt(dayEndField.getText());
                    mon = Integer.parseInt(monEndField.getText());
                    yea = Integer.parseInt(yearEndField.getText());
                    endD = new Date(yea, mon - 1, day);
                    logger.debug("END-DATE=" + endD.dateToString(false));
                    if (endD.get(java.util.Calendar.YEAR) != yea
                            || endD.get(java.util.Calendar.MONTH) != mon - 1
                            || endD.get(java.util.Calendar.DAY_OF_MONTH) != day)
                        throw new InvalidDateException("Endedatum");

                    if (endD.sameDateAs(startD))
                        endD = null;
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Das Datum enthält ungültige Zeichen.",
                    "Ungültige Eingaben...", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (InvalidDateException e) {
            JOptionPane.showMessageDialog(this,
                    "Das eingegebene " + e.getMessage() + " existiert nicht.",
                    "Ungültiges Datum...", JOptionPane.ERROR_MESSAGE);
            return;
        }

        /*
         * Now check valid 'Name'
         */
        if (nameField.getText().length() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Der Name darf nicht leer sein.", "Leeres Feld...",
                    JOptionPane.ERROR_MESSAGE);
            return;
        } 
        else if (nameField.getText().length() > MAX_LEN_NAME) {
            JOptionPane.showMessageDialog(this,
                    "Der Name darf 30 Zeichen nicht überschreiten!\nNutze \"Notizen\" für ausführlichere Beschreibungen.",
                    "Name zu lang...", JOptionPane.ERROR_MESSAGE);
            return;
        }

        /*
         * In case name and Dates are OK...
         */
        if (timeBox.isSelected()) {
            startD.set(java.util.Calendar.HOUR_OF_DAY, hoursBox.getSelectedIndex());
            startD.set(java.util.Calendar.MINUTE, minutesBox.getSelectedIndex());
            startD.setHasTime(true);
        } 
        else
            startD.setHasTime(false);

        logger.debug("INDEX=" + remindBox.getSelectedIndex());
        short freq = Frequency.OCCUR_ONCE;
        if (freq1.isSelected())
            freq = Frequency.bool2short(wBox.isSelected(), mBox.isSelected(), yBox.isSelected());
        else if (freq2.isSelected())
            freq = Frequency.OCCUR_BY_WEEKDAY;
        else if (freq3.isSelected())
            freq = Frequency.genByInterval(intervalBox.getSelectedIndex() + 1, 
                    unitBox.getSelectedIndex());
        else if (freq4.isSelected())
            freq = Frequency.OCCUR_BY_MONTHEND;
        
        Event newEvent = new Event(startD, endD, nameField.getText(), Event.HOLIDAY_NONE,
                freq, (byte) remindBox.getSelectedIndex(),
                event.getID() == -1 || copy ? caller.genID() : event.getID());

        /*
         * Write notes to a file
         */
        if (notesField.getText().length() != 0) {
            try {
                String dirname = caller.getPath(Const.EVENT_DIR);
                new File(dirname).mkdir();
                new File(dirname += File.separator + newEvent.getID()).mkdir();

                File notes_txt = new File(dirname + File.separator
                        + Const.NOTES_FILE);
                notes_txt.createNewFile();
                if (!notes_txt.canWrite()) {
                    JOptionPane.showMessageDialog(this,
                            "Der Kalender hat hier keine Schreibrechte.",
                            "Keine Schreibrechte...", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(notes_txt)));

                out.write(notesField.getText());
                out.close();
            } catch (Exception e) {
                logger.error("error while trying to write notes to a file...", e);
            }
        }

        /*
         * Is there an old attachment to delete?
         */
        File oldAttachment = event.getAttachment(caller.getWorkspace());
        if (oldAttachment != null && !copy) {
            logger.debug("old attachment was:" + oldAttachment.getPath());
            logger.debug("new file=" + attachField.getText());
            if (!oldAttachment.getPath().equals(attachField.getText())) {
                if (!event.attachmentIsLink(caller.getWorkspace())) {
                    if (JOptionPane.showConfirmDialog(this,
                            "Der bestehende Anhang dieses Ereignisses\nwar eine Kopie einer Datei.\nSoll diese Kopie wirklich gelöscht werden?",
                            "Alten Anhang löschen...", JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION)
                        return;
                    event.getAttachment(caller.getWorkspace()).delete();
                }
                new File(caller.getPath(Const.EVENT_DIR) + File.separator + event.getID()
                        + Const.LINK_FILE).delete();
            }
        }

        /*
         * Attach file
         */
        if (attachField.getText().length() != 0) {
            String dirname = caller.getPath(Const.EVENT_DIR);
            new File(dirname).mkdir();
            new File(dirname += File.separator + newEvent.getID()).mkdir();

            boolean asLink;
            Object[] options = { "Link", "Kopie", "Abbruch" };
            int auswahl = JOptionPane.showOptionDialog(caller.getGUI().getFrame(),
                    "Soll der neue Anhang ein Link auf die ausgewählte Datei sein\noder soll eine Kopie erstellt werden?\n\nLinks sind speichereffizient, aber wenn die Originaldatei\nverschoben oder gelöscht wird, funktioniert der Link nicht mehr.",
                    "Neuer Anhang...", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (auswahl == 0)
                asLink = true;
            else if (auswahl == 1)
                asLink = false;
            else
                return;

            if (asLink) {
                /*
                 * Create a little text file with the link as content
                 */
                try {
                    File link_txt = new File(dirname + File.separator
                            + Const.LINK_FILE);
                    link_txt.createNewFile();
                    if (!link_txt.canWrite()) {
                        JOptionPane.showMessageDialog(this,
                                "Der Kalender hat hier keine Schreibrechte.",
                                "Keine Schreibrechte...",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    BufferedWriter out = new BufferedWriter(
                            new OutputStreamWriter(new FileOutputStream(
                                    link_txt), Const.ENCODING));

                    out.write(attachField.getText());
                    out.close();
                } catch (Exception e) {
                    logger.error("error while trying to create link file...", e);
                }
            } 
            else {
                /*
                 * Copy the complete file
                 */
                File orig = new File(attachField.getText());
                File copy = new File(dirname + File.separator + orig.getName());
                new Copy(caller.getGUI().getFrame(), orig, copy);
            }
        }

        /*
         * Nothing additional to attach -> so try deleting the folder
         */
        else if (notesField.getText().length() == 0
                && attachField.getText().length() == 0)
            new File(caller.getPath(Const.EVENT_DIR) + File.separator + 
                    event.getID()).delete();

        if (copy)
            caller.newEvent(newEvent);
        else
            caller.editEvent(event.getID(), newEvent);

        windowClosing(null);
    }

    /**
     * Extend or reduce the "edit event" frame.
     * @param full - True to extend, false to reduce
     */
    public void extend(boolean full) {
        if (full)
            add(extendedSettingsPanel, BorderLayout.CENTER);
        else
            remove(extendedSettingsPanel);
        
        pack();
        setLocationRelativeTo(caller.getGUI().getFrame());
    }

    @Override
    public void keyReleased(KeyEvent k) {
        if (k.getKeyCode() == KeyEvent.VK_ENTER)
            submit();
        else if (k.getKeyCode() == KeyEvent.VK_ESCAPE)
            windowClosing(null);
    }

    @Override
    public void keyPressed(KeyEvent k) {
    }

    @Override
    public void keyTyped(KeyEvent k) {
    }

    @Override
    public void itemStateChanged(ItemEvent i) {
        if (i.getSource().equals(timeBox)) {
            hoursBox.setEnabled(timeBox.isSelected());
            minutesBox.setEnabled(timeBox.isSelected());
            clockLabel.setEnabled(timeBox.isSelected());
            dotsLabel.setEnabled(timeBox.isSelected());
        }
        else if (i.getSource().equals(freq1)) {
            wBox.setEnabled(true);
            mBox.setEnabled(true);
            yBox.setEnabled(true);
            intervalBox.setEnabled(false);
            unitBox.setEnabled(false);
            byWeekdayLabel.setEnabled(false);
            byIntervalLabel.setEnabled(false);
            byEndOfMonthLabel.setEnabled(false);
        }
        else if (i.getSource().equals(freq2)) {
            wBox.setEnabled(false);
            mBox.setEnabled(false);
            yBox.setEnabled(false);
            intervalBox.setEnabled(false);
            unitBox.setEnabled(false);
            byWeekdayLabel.setEnabled(true);
            byIntervalLabel.setEnabled(false);
            byEndOfMonthLabel.setEnabled(false);
        }
        else if (i.getSource().equals(freq3)) {
            wBox.setEnabled(false);
            mBox.setEnabled(false);
            yBox.setEnabled(false);
            intervalBox.setEnabled(true);
            unitBox.setEnabled(true);
            byWeekdayLabel.setEnabled(false);
            byIntervalLabel.setEnabled(true);
            byEndOfMonthLabel.setEnabled(false);
        }
        else if (i.getSource().equals(freq4)) {
            wBox.setEnabled(false);
            mBox.setEnabled(false);
            yBox.setEnabled(false);
            intervalBox.setEnabled(false);
            unitBox.setEnabled(false);
            byWeekdayLabel.setEnabled(false);
            byIntervalLabel.setEnabled(false);
            byEndOfMonthLabel.setEnabled(true);
        }
        else if (i.getSource().equals(attachmentBox)) {
            if (!attachmentBox.isSelected()) {
                attachField.setText("");
                attachField.setEnabled(false);
                chooseButton.setEnabled(false);
            }
            else {
                attachField.setEnabled(true);
                chooseButton.setEnabled(true);
                
                attach();
                
                /* ??? */
                if (attachField.getText().length() != 0) {
                    attachmentBox.removeItemListener(this);
                    attachmentBox.setSelected(true);
                    chooseButton.setEnabled(true);
                    attachField.setEnabled(true);
                    attachmentBox.addItemListener(this);
                }
            }
        }
    }

    @Override
    public void windowDeactivated(WindowEvent w) {
    }

    @Override
    public void windowActivated(WindowEvent w) {
    }

    @Override
    public void windowDeiconified(WindowEvent w) {
    }

    @Override
    public void windowIconified(WindowEvent w) {
    }

    @Override
    public void windowClosed(WindowEvent w) {
    }

    @Override
    public void windowClosing(WindowEvent w) {
        caller.setNotisVisible(true);
        setVisible(false);
        dispose();
    }

    @Override
    public void windowOpened(WindowEvent w) {
    }

    @Override
    public void mouseExited(MouseEvent m) {
        Cursor c = new Cursor(Cursor.DEFAULT_CURSOR);
        setCursor(c);
    }

    @Override
    public void mouseEntered(MouseEvent m) {
        Cursor c = new Cursor(Cursor.HAND_CURSOR);
        setCursor(c);
    }

    @Override
    public void mouseClicked(MouseEvent m) {
        if (m.getSource().equals(extendLabel))
            extendButton.setPressed(!extendButton.isPressed());
    }

    @Override
    public void mousePressed(MouseEvent m) {
    }

    @Override
    public void mouseReleased(MouseEvent m) {
    }

    @Override
    public void buttonPressed(ImageButton x) {
        this.extendLabel.setText(x.isPressed() ? REDUCE : EXTEND);
        this.extend(x.isPressed());
    }
}
