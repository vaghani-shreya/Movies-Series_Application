package front;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class netflix extends JPanel {
	private static JPanel showPanel;
	private JScrollPane scrollPane;
	private JComboBox<String> filterComboBox;
	private JComboBox<String> sortComboBox;
	private static netflix instance;

	private netflix() {
		initComponents();
		NetflixDataBase();
	}
	public static netflix getInstance() {
		if (instance == null)
			instance = new netflix();
		return instance;
	}

	private void initComponents(){
		//		setTitle("Netflix Discover Weekly");
		//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 600);
		//		setLocationRelativeTo(null);
		//create a search panel
		JPanel searchPanel = new JPanel();
		JTextField searchField = new JTextField(20);
		// create search button
		JButton searchButton = new JButton("Search");
		// create search button
		JButton sortButton = new JButton("Sort");

		// Filter menu feature
		String[] filterOptions = {"Title", "Genre", "Type", "Ratings"};
		filterComboBox = new JComboBox<>(filterOptions);

		// Sort menu feature
		String[] sortOptions = {"Release Date", "Title", "Date Added"};
		sortComboBox = new JComboBox<>(sortOptions);

		searchPanel.add(searchField);
		searchPanel.add(filterComboBox);
		searchPanel.add(searchButton);
		searchPanel.add(new JLabel("Sort By:"));
		searchPanel.add(sortComboBox);
		searchPanel.add(sortButton);


		// Add action listener to the search button
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String searchFor = searchField.getText();
				String selectedFilter = (String) filterComboBox.getSelectedItem();
				searchNetflixDatabase(searchFor, selectedFilter);
			}
		});

		// Add action listener to the sort button
		sortButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String selectedSort = (String) sortComboBox.getSelectedItem();
				sortNetflixDatabase(selectedSort);
			}
		});

		scrollPane = new JScrollPane();
		showPanel = new JPanel();
		showPanel.setLayout(new BoxLayout(showPanel, BoxLayout.Y_AXIS));
		scrollPane.setViewportView(showPanel);

		// Add components to this JPanel
		setLayout(new BorderLayout());
		add(searchPanel, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);

	}

	// Allows the user to see the shows in descending order of release year
	public static void NetflixDataBase() {
		String path = "jdbc:sqlite:database/Netflix.db";
		// extract data from netflix database by descending order in terms of release year
		String query = "SELECT * FROM netflix_titles ORDER BY release_year DESC LIMIT 10;";


		try {
			//call the JDBC driver
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection(path);
			Statement stmt = conn.createStatement();

			ResultSet resultSet = stmt.executeQuery(query);

			//loop through all data required from the database
			while (resultSet.next()) {
				String id = resultSet.getString("show_id");
				String title = resultSet.getString("title");
				String dateAdded = resultSet.getString("date_added");
				String releaseYear = resultSet.getString("release_year");
				String director = resultSet.getString("director");
				String cast = resultSet.getString("cast");
				String description = resultSet.getString("description");
				String date_added = resultSet.getString("date_added");
				//prints the specified show / movie and the corresponding information
				JLabel showLabel = new JLabel("Show ID: " + id + ", Title: " + title + ", Date Added: " + dateAdded + ", Release Year: " + releaseYear);
				showLabel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						showDetails(id, title, dateAdded, releaseYear,director, description, cast, date_added);
					}
				});
				showLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				showLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				showPanel.add(showLabel);
				showPanel.add(Box.createVerticalStrut(10)); // Add vertical spacing between shows
			}

			conn.close();
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	// Allows the user to search for a specific show/movie
	private void searchNetflixDatabase(String searchFor, String filterBy) {
		showPanel.removeAll(); // Clear existing shows/movies

		String path = "jdbc:sqlite:database/Netflix.db";
		//Finds the specified title and extracts from database
		//		String query = "SELECT * FROM netflix_titles WHERE title LIKE ?;";
		String query;

		switch(filterBy.toLowerCase()) {
		case "title":
			query = "SELECT * FROM netflix_titles WHERE title LIKE ? LIMIT 10;";
			break;
		case "genre":
			query = "SELECT * FROM netflix_titles WHERE listed_in LIKE ? LIMIT 10;";
			break;
		case "type":
			query = "SELECT * FROM netflix_titles WHERE type LIKE ? LIMIT 10;";
			break;
		case "rating":
			query = "SELECT * FROM netflix_titles WHERE rating LIKE ? LIMIT 10;";
			break;
		default:
			query = "SELECT * FROM netflix_titles WHERE title LIKE ? LIMIT 10;";
			break;
		}

		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection(path);
			PreparedStatement pstmt = conn.prepareStatement(query);
			pstmt.setString(1, "%" + searchFor + "%");

			ResultSet resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				String id = resultSet.getString("show_id");
				String title = resultSet.getString("title");
				String dateAdded = resultSet.getString("date_added");
				String releaseYear = resultSet.getString("release_year");
				String director = resultSet.getString("director");
				String cast = resultSet.getString("cast");
				String description = resultSet.getString("description");
				String date_added = resultSet.getString("date_added");
				//prints the specified show / movie and the corresponding information

				JLabel showLabel = new JLabel("ID: " + id + ", Title: " + title + ", Date Added: " + dateAdded + ", Release Year: " + releaseYear);
				showLabel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						showDetails(id, title, dateAdded, releaseYear, director, description, cast, date_added);
					}
				});

				showLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				showLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				showPanel.add(showLabel);
				showPanel.add(Box.createVerticalStrut(10)); 
			}

			conn.close();
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		showPanel.revalidate(); // Refresh layout
		showPanel.repaint(); // Repaint the panel
	}

	// Allows the user to sort show/movie by criteria
	private void sortNetflixDatabase(String sortBy) {
		String path = "jdbc:sqlite:database/Netflix.db";
		String query;

		switch (sortBy.toLowerCase()) {
		case "title":
			query = "SELECT * FROM netflix_titles ORDER BY title DESC LIMIT 10;";
			break;
		case "release date":
			query = "SELECT * FROM netflix_titles ORDER BY release_year DESC LIMIT 10;";
			break;
		case "date added":
			query = "SELECT * FROM netflix_titles ORDER BY date_added DESC LIMIT 10;";
			break;
		default:
			query = "SELECT * FROM netflix_titles ORDER BY release_year DESC LIMIT 10;";
			break;
		}
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection(path);
			Statement stmt = conn.createStatement();

			ResultSet resultSet = stmt.executeQuery(query);
			showPanel.removeAll();

			while (resultSet.next()) {
				String id = resultSet.getString("show_id");
				String title = resultSet.getString("title");
				String dateAdded = resultSet.getString("date_added");
				String releaseYear = resultSet.getString("release_year");
				String director = resultSet.getString("director");
				String cast = resultSet.getString("cast");
				String description = resultSet.getString("description");
				String date_added = resultSet.getString("date_added");
				//prints the specified show / movie and the corresponding information

				JLabel showLabel = new JLabel("ID: " + id + ", Title: " + title + ", Date Added: " + dateAdded + ", Release Year: " + releaseYear);
				showLabel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						showDetails(id, title, dateAdded, releaseYear, director, description, cast, date_added);
					}
				});

				showLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				showLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				showPanel.add(showLabel);
				showPanel.add(Box.createVerticalStrut(10)); 
			}

			conn.close();
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		showPanel.revalidate(); // Refresh layout
		showPanel.repaint(); // Repaint the panel
	}



	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new netflix().setVisible(true);

			}
		});
	}




	private static void showDetails(String showId, String title, String dateAdded, String releaseYear, String director, String description, String cast, String date_added) {
		String username = LoginPage.getUsernameForDB();
		Favourites favourites = new Favourites();
		WatchHistory watchedList = new WatchHistory();

		// Open a new page to display more details about a specific show/movie
		JFrame detailsFrame = new JFrame("Show Details");
		JPanel detailsPanel = new JPanel(); // Use a grid layout
		detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
		JTextArea detailsTextArea = new JTextArea();
		detailsTextArea.append("Show ID: " + showId + "\n");
		detailsTextArea.append("Title: " + title + "\n");
		detailsTextArea.append("Date Added: " + dateAdded + "\n");
		detailsTextArea.append("Release Year: " + releaseYear + "\n");
		detailsTextArea.append("Director: " + director + "\n");
		detailsTextArea.append("Description : " + description + "\n");
		detailsTextArea.append("Cast: " + cast + "\n");

		JButton likeButton = new JButton("Add to Favourites");
		likeButton.setPreferredSize(new Dimension(20,40));
		likeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Call addToFavouritesList method when the button is clicked
				favourites.addToFavouritesList(username,showId, title, dateAdded, releaseYear, director, cast, description);
			}
		});

		//Mark as Watched/Watched Button
		JButton watchedButton = new JButton("");
		//call db to check if movie is in watch history
		boolean watch = watchedList.checkWatchList(username, showId, title);
		//If statement to show if the button is "Mark as Watched" or "Watched"
		if(watch == true) {
			watchedButton.setText("Watched");

		} else {
			watchedButton.setText("Mark as Watched");

		}
		watchedButton.setPreferredSize(new Dimension(20,40));
		// Set the button's bounds (x, y, width, height)
		watchedButton.setBounds(50, 50, 100, 30);
		watchedButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(watchedList.checkWatchList(username, showId, title) == true) {
					watchedList.deleteShowFromWatchList(username, showId, title);
					watchedButton.setText("Mark As Watched");

				} else {
					watchedList.addToWatchedList(username, showId, title);
					watchedButton.setText("Watched");

				}
			}
		});

		watch = watchedList.checkWatchList(username, showId, title);

		detailsPanel.add(likeButton);
		detailsPanel.add(watchedButton);
		detailsPanel.add(detailsTextArea);
		detailsFrame.add(detailsPanel);

		detailsFrame.setSize(300, 200);
		detailsFrame.setLocationRelativeTo(null);
		detailsFrame.setVisible(true);
	}


}


