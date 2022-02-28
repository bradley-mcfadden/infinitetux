package com.mojang.mario.mapedit;

import java.awt.Component;
import java.awt.GridLayout;

import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import com.mojang.mario.Art;

/**
 * MessagePanel provides an interface for displaying messages to the user that are
 * not immediately important enough to be modal.
 */
public class MessagePanel extends JPanel {
    private JList<Message> list;
    private DefaultListModel<Message> listModel;
    private HashMap<String, Message> keyedMessages;

    /**
     * Constructor
     */
    public MessagePanel() 
    {
        setLayout(new GridLayout(0, 1));
        setBorder(BorderFactory.createTitledBorder("Messages"));
        keyedMessages = new HashMap<>();
        listModel = new DefaultListModel<>();
        list = new JList<>(listModel);
        list.setVisibleRowCount(5);
        list.setCellRenderer(new MessageCellRenderer());
        add(new JScrollPane(list));
    }

    /**
     * addMessage to the list
     * @param message Message object to add
     */
    public void addMessage(Message message) 
    {
        listModel.addElement(message);
    }

    /**
     * addMessage of type and text to the list
     * @param type @see Message
     * @param text Text to go with the message
     */
    public void addMessage(int type, String text) 
    {
        listModel.addElement(new Message(type, text));
    }

    /**
     * addKeyedMessage adds message referenced by key. 
     * Duplicating a key replaces the text of the current message
     * with the key in place.
     * @param key Key to remember message by. Should be non-null
     * @param type @see Message
     * @param text Text to go with message
     */
    public void addKeyedMessage(String key, int type, String text) {
        if (keyedMessages.containsKey(key)) {
            Message tmpMessage = keyedMessages.get(key);
            tmpMessage.type = type;
            tmpMessage.text = text;

            listModel.setElementAt(tmpMessage, listModel.indexOf(tmpMessage));
        } else {
            Message tmpMessage = new Message(type, text);
            addMessage(tmpMessage);
            keyedMessages.put(key, tmpMessage);
        }
    }

    /**
     * removeKeyedMessage remove a message by its key.
     * @param key Key to remove message with
     */
    public void removeKeyedMessage(String key) {
        if (keyedMessages.containsKey(key)) {
            Message tmpMessage = keyedMessages.get(key);
            keyedMessages.remove(key);
            listModel.removeElement(tmpMessage);
        }
    }

    /**
     * removeMessage
     * @param message message to remove from display
     */
    public void removeMessage(Message message)
    {
        listModel.removeElement(message);
    }

    /**
     * updateMessage change the referenced message's text
     * @param message Message to update text of
     */
    public void updateMessage(Message message)
    {
        int index = listModel.indexOf(message);
        if (index != -1) {
            listModel.remove(index);
            listModel.insertElementAt(message, index);
        } else {
            listModel.addElement(message);
        }       
    }

    /**
     * Message represents a message that the user should see.
     * Associated with a message is a type.
     * Type can be one of WARNING, ERROR, SUGGESTION, or INFO
     */
    public static class Message 
    {
        public static final int WARNING = 1;
        public static final int ERROR = 2;
        public static final int SUGGESTION = 3;
        public static final int INFO = 4;

        private int type;
        private String text;

        /**
         * Constructor
         * @param type
         * @param text
         */
        public Message(int type, String text) {
            this.type = type;
            this.text = text;
        }
    }

    private static class MessageCellRenderer extends JLabel implements ListCellRenderer<Message> 
    {
        @Override
        public Component getListCellRendererComponent(JList<? extends Message> list, Message value, int index,
                boolean isSelected, boolean cellHasFocus) {
            String s = value.text;
            setText(s);
            int type = value.type;

            Icon icon = null;
            switch (type) {
                case Message.INFO:
                    icon = new ImageIcon(Art.info);
                    break;
                case Message.SUGGESTION:
                    icon = new ImageIcon(Art.suggestion);
                    break;
                case Message.WARNING:
                    icon = new ImageIcon(Art.warning);
                    break;
                case Message.ERROR:
                    icon = new ImageIcon(Art.error);
                    break;
            }
            
            setIcon(icon);
            setBackground(list.getBackground());
            setForeground(list.getForeground());
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
    }

    public static void main (String[] args)
    {
        MessagePanel messagePanel = new MessagePanel();
        MessagePanel.Message[] messages = new Message[]{
            new Message(Message.ERROR, "Your refrigerator is running!"),
            new Message(Message.SUGGESTION, "I think you should start working out"),
            new Message(Message.WARNING, "Do not buy video games without reading about technical issues")
        }; 
        JFrame frame = new JFrame("MessagePanel Test");
        Art.init(frame.getGraphicsConfiguration(), null);
        frame.add(messagePanel);
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        for (Message message : messages) {
            System.out.println("Looping through panel");
            messagePanel.addMessage(message);
        }
    }
}
