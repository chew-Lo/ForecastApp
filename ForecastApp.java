
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.Map.*;
import java.util.regex.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 *
 * This a CSCI 2021 project that fetches and displays the current weather conditions for a given city.
 *
 * @author Kyle Lofredo
 * @version 20
 *
 */

public class ForecastApp extends JFrame {

    private JTextField cityField;
    private JTextArea weatherArea;
    private JButton fetchButton;
    private JLabel titleLabel;
    private JPanel mainPanel, inputPanel, resultPanel;
    private JScrollPane scrollPane;

    public ForecastApp() {
        // This will set up the main application window - made taller for better display
        setTitle("Weather Forecast Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 700); // Increased height from 500 to 700
        setLocationRelativeTo(null);
        
        // This is how we initialize the GUI components
        initComponents();
        
        // This will arrange the components in the window
        layoutComponents();
        
        // This will make the window visible
        setVisible(true);
    }

    /**
     * This is how we create and configure the GUI components
     */
    private void initComponents() {
        // This will create the input field for the city name
        cityField = new JTextField(20);
        
        // This will create the button to fetch weather data
        fetchButton = new JButton("Get Weather");
        fetchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // This will handle the button click event
                String location = cityField.getText().trim();
                if (!location.isEmpty()) {
                    // This will fetch and display the weather data
                    showWeather(location);
                } else {
                    // This will show an error message if no city is entered
                    JOptionPane.showMessageDialog(ForecastApp.this, 
                            "Please enter a city name", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // This will create the area to display weather information - made taller
        weatherArea = new JTextArea(15, 40); // Set specific rows and columns
        weatherArea.setEditable(false);
        weatherArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        weatherArea.setLineWrap(true);
        weatherArea.setWrapStyleWord(true);
        
        // This will add a scroll pane to the text area with preferred size
        scrollPane = new JScrollPane(weatherArea);
        scrollPane.setPreferredSize(new Dimension(550, 350)); // Set preferred size
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // This will create the title label
        titleLabel = new JLabel("Live Weather Information", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        // This will create the panels for organizing components
        mainPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        resultPanel = new JPanel(new BorderLayout());
        
        // This will add padding around the main panel - reduced top padding
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10)); // Reduced from 100 to 20
    }

    /**
     * This is how we arrange the components in the window
     */
    private void layoutComponents() {
        // This will add components to the input panel
        inputPanel.add(new JLabel("Enter a city name:"));
        inputPanel.add(cityField);
        inputPanel.add(fetchButton);
        
        // This will add components to the result panel
        resultPanel.add(new JLabel("Weather Information:", JLabel.CENTER), BorderLayout.NORTH);
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        
        // This will add all panels to the main panel
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(resultPanel, BorderLayout.SOUTH);
        
        // This will set the main panel as the content pane
        setContentPane(mainPanel);
    }

    /**
     * This method gets the weather and the location of the city through the API
     * @param location this is the name of the location relative to the url argument
     */
    public void showWeather(String location) {
        // This will show a loading message while fetching data
        weatherArea.setText("Fetching weather data for " + location + "...");
        
        // This will use a separate thread to avoid freezing the GUI
        new Thread(() -> {
            try {
                // This will encode the location to handle spaces and special characters
                String encodedLocation = URLEncoder.encode(location, "UTF-8");
                String key = "1516fe41c920442babd110027200105&q=" + encodedLocation;
                String url = "http://api.weatherapi.com/v1/current.json?key=" + key;

                // This is how we connect to the Weather API to fetch data
                URL link = new URL(url);
                BufferedReader buffer = new BufferedReader(new InputStreamReader(link.openStream()));
                String buff;
                buff = buffer.readLine();

                String data[] = buff.split(",");
                Map<String, String> map = new HashMap<String, String>();

                // This is the main loop that processes the API response
                for (int i = 1; i < data.length; i++) {
                    // This is how we extract the region information
                    if (Pattern.compile(".region*").matcher(data[i]).find()) {
                        data[i] = data[i].substring(data[i].indexOf(":") + 1, data[i].length());
                        map.put("Region", data[i]);
                    }
                    // This is how we extract the country information
                    else if (Pattern.compile(".country*").matcher(data[i]).find()) {
                        data[i] = data[i].substring(data[i].lastIndexOf(":") + 1, data[i].length());
                        map.put("Country", data[i]);
                    }
                    // This is how we extract the local time information
                    else if (Pattern.compile(".localtime[^a-z]*").matcher(data[i]).find()) {
                        data[i] = data[i].substring(data[i].indexOf(":") + 1, data[i].length() - 1);
                        map.put("Localtime", data[i]);
                    }
                    // This is how we extract the temperature information
                    else if (Pattern.compile(".*temp_f*").matcher(data[i]).find()) {
                        data[i] = data[i].substring(data[i].lastIndexOf(":") + 1, data[i].length());
                        map.put("Temperature (Fahrenheit)", data[i]);
                    }
                    // This is how we determine if it's day or night
                    else if (Pattern.compile(".is_day*").matcher(data[i]).find()) {
                        if (data[i].charAt(data[i].length() - 1) == '1')
                            map.put("Day/Night", "Day");
                        else
                            map.put("Day/Night", "Night");
                    }
                    // This is how we extract the weather condition information
                    else if (Pattern.compile(".*text*").matcher(data[i]).find()) {
                        data[i] = data[i].substring(data[i].lastIndexOf(':') + 1, data[i].length());
                        map.put("Condition", data[i]);
                    }
                }

                // This will build the result string to display
                StringBuilder result = new StringBuilder();
                result.append("Weather for: ").append(location).append("\n\n");
                for (Entry<String, String> e : map.entrySet()) {
                    result.append(e.getKey()).append(" : ").append(e.getValue()).append("\n\n");
                }

                // This will update the text area with the weather information
                SwingUtilities.invokeLater(() -> {
                    weatherArea.setText(result.toString());
                });

            } catch (Exception e) {
                // This will handle any errors that occur during the API request
                SwingUtilities.invokeLater(() -> {
                    weatherArea.setText("Error fetching weather data: " + e.getMessage());
                });
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * This is the main method that starts the application
     * This will launch the GUI weather application
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        // This will ensure the GUI is created on the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ForecastApp();
            }
        });
    }
}