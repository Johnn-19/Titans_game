package game.gui;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.*;
import java.io.IOException;
import java.util.*;
import javafx.animation.*;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.scene.control.CheckBox;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import game.engine.Battle;
import game.engine.exceptions.InsufficientResourcesException;
import game.engine.exceptions.InvalidLaneException;
import game.engine.titans.AbnormalTitan;
import game.engine.titans.Titan;
import game.engine.weapons.PiercingCannon;
import game.engine.weapons.Weapon;
import game.engine.lanes.*;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import game.engine.weapons.*;

public class GUI extends Application {
	
	private static ArrayList<VBox> walls = new ArrayList<VBox>();
	private static ArrayList<ProgressBar> healthBars = new ArrayList<ProgressBar>();
	private static ArrayList<VBox> wallWeaponsList = new ArrayList<VBox>();
	private static HashMap<Lane, ProgressBar> laneProgBars = new HashMap<Lane,ProgressBar>();
	private static ArrayList<Titan> allTitans = new ArrayList<Titan>();
	private static ArrayList<Pane> lanePanes = new ArrayList<Pane>();
	private static HashMap<Lane, Pane> lanePanesHash = new HashMap<Lane, Pane>();
	private static HashMap<Lane,Label> laneLabels = new HashMap<Lane,Label>();
	
	private static HashMap<Titan, VBox> titanFigs = new HashMap<Titan,VBox>();
	private static HashMap<Titan,ProgressBar> titanHealthBars = new HashMap<Titan,ProgressBar>();
	
	private static Battle control;
	private static HBox scoresList = new HBox();
	private static ArrayList<RadioButton> radioButtons = new ArrayList<RadioButton>();
	private static HashMap<RadioButton,Lane> buttonLaneHash = new HashMap<RadioButton,Lane>();
	private static ToggleGroup toggleGrp = new ToggleGroup();

	
	
	public static VBox makeWall(Lane l) {
	    ProgressBar wallHealthBar = new ProgressBar();
	    wallHealthBar.setProgress(1.0);
	    wallHealthBar.setPrefSize(100, 10);
	    
	    healthBars.add(wallHealthBar);
	    laneProgBars.put(l, wallHealthBar);
	    
	    VBox wallWeapons = new VBox();
	    wallWeapons.setSpacing(5);
	    wallWeapons.setMaxHeight(50); // Set max height to 50 pixels
	    
	    ScrollPane scrollPane = new ScrollPane(wallWeapons);
	    scrollPane.setFitToWidth(true);
	    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); 

	    wallWeaponsList.add(wallWeapons);
	    
	    VBox wall = new VBox();
	    wall.getChildren().addAll(wallHealthBar, scrollPane); // Add scrollPane to the wall
	    return wall;
	}
	
	public static Pane makeLane() {
		Rectangle rectangle = new Rectangle(650,90);
		rectangle.setFill(Color.TRANSPARENT);
		rectangle.setStroke(Color.TRANSPARENT);
		
		
		
		Pane lanePane = new Pane();
		lanePane.getChildren().add(rectangle);
		return lanePane;
	}
	
	public static GridPane constructLanes() {

		GridPane laneGrid = new GridPane();
		laneGrid.setPadding(new Insets(40));
		
		for(int i=0; i<control.getLanes().size(); i++) {
			VBox wall = makeWall(control.getOriginalLanes().get(i));
			Pane lanePane = makeLane();
			laneGrid.setGridLinesVisible(true);
			
			laneGrid.add(wall,0,i);
			laneGrid.add(lanePane, 1, i);
			
			lanePanesHash.put(control.getOriginalLanes().get(i),lanePane);
			lanePanes.add(lanePane);
			
			Label dangerLevel = new Label("" + control.getOriginalLanes().get(i).getDangerLevel());
			dangerLevel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 18));
			dangerLevel.setTranslateX(657);
			dangerLevel.setTranslateY(34);
			dangerLevel.setTextFill(Color.GREEN);
			lanePane.getChildren().add(dangerLevel);
			laneLabels.put(control.getOriginalLanes().get(i),dangerLevel);
		}
		
		makeRadioButtons();
		
		return laneGrid;
	}
	
	public static void spawnTitan(Pane lanePane) {
	
        Circle titanFig = new Circle();
        titanFig.setRadius(10);
        titanFig.setFill(Color.RED);
        
        double moveShiftX = 625; //lane lost when zero, spawns at 625
        double moveShiftY = 55;
//      double circleCenterX = moveShiftX;
//      double circleCenterY = moveShiftY;
        
        titanFig.setCenterX(moveShiftX);
        titanFig.setCenterY(moveShiftY);
  
  
        lanePane.getChildren().addAll(titanFig);
	}
	
	public static void updateScores() {

		Label l1 = new Label("Score: " + control.getScore());
		Label l2 = new Label("   Turn: " + control.getNumberOfTurns());
		Label l3 = new Label("   Phase:  " + control.getBattlePhase());
		Label l4 = new Label("   Resources: " + control.getResourcesGathered());
		Label l5 = new Label("   Available Lanes: " + control.getLanes().size());
		l1.setFont(Font.font(15));
		l2.setFont(Font.font(15));
		l3.setFont(Font.font(15));
		l4.setFont(Font.font(15));
		l5.setFont(Font.font(15));	
		scoresList.getChildren().clear();
		scoresList.getChildren().addAll(l1,l2,l3,l4,l5);
		scoresList.setPadding(new Insets(20));
		
	}
	
	public static void updateWallProgBars() {
		for(int i = 0; i<control.getOriginalLanes().size(); i++) {
			Lane l = control.getOriginalLanes().get(i);
			laneProgBars.get(l).setProgress(l.getLaneWall().getCurrentHealth()/l.getLaneWall().getBaseHealth());
			
			if(l.getLaneWall().getCurrentHealth()/l.getLaneWall().getBaseHealth()<=0) {
				Rectangle rectangle = new Rectangle(650,90);
				rectangle.setFill(Color.GRAY);
				rectangle.setStroke(Color.GRAY);
				lanePanesHash.get(l).getChildren().add(rectangle);
			}
				
		}
	}
	
	public static void updateTitansProgBars() {
	    ArrayList<Lane> origLanes = control.getOriginalLanes();
	    
	    for (int i = 0; i < control.getOriginalLanes().size(); i++) {
	        PriorityQueue<Titan> laneTitans = origLanes.get(i).getTitans();
	        PriorityQueue<Titan> titans = new PriorityQueue<>();
	        
	        for(Titan t: laneTitans)
	        	titans.add(t);
	        
	        for (Titan t : titans) {
	            ProgressBar prog = titanHealthBars.get(t);
	  
	            if (prog != null) {
	            	double pr = (double) t.getCurrentHealth()/t.getBaseHealth();
	                prog.setProgress(pr);
	                
	                
	            } else {
	                System.err.println("ProgressBar not found for Titan: " + t);
	            }
	            
	        }
	        
	    }
	}
	
	public static void updateDangerLevel() {
		for(int i = 0; i<control.getOriginalLanes().size();i++) {
			
			Label label = laneLabels.get(control.getOriginalLanes().get(i));
			label.setText((""+control.getOriginalLanes().get(i).getDangerLevel()));
			}
	}
	
	public static void addWeapon(int code, int laneNumber) {
		ImageView imageView = new ImageView(); 
		if (code == 1) {
	        try {
	            Image image = new Image("file:Piercing Cannon.png");
	            imageView = new ImageView(image);
	            imageView.setFitWidth(70); 
	            imageView.setPreserveRatio(true);
	            
	        } catch (Exception e) {
	            System.out.println("Image not found: " + e.getMessage());
	        }
	    }
		
		if(code==2) {
			 try {
		            Image image = new Image("file:Sniper Cannon.png");
		            imageView = new ImageView(image);
		            imageView.setFitWidth(70); 
		            imageView.setPreserveRatio(true);
		            
		        } catch (Exception e) {
		            System.out.println("Image not found: " + e.getMessage());
		        }
		}
		
		if(code==3) {
			 try {
		            Image image = new Image("file:VolleySpread Cannon.png");
		            imageView = new ImageView(image);
		            imageView.setFitWidth(70); 
		            imageView.setPreserveRatio(true);
		            
		        } catch (Exception e) {
		            System.out.println("Image not found: " + e.getMessage());
		        }
		}
		
		if(code==4) {
			 try {
		            Image image = new Image("file:Wall Trap.png");
		            imageView = new ImageView(image);
		            imageView.setFitWidth(70); 
		            imageView.setPreserveRatio(true);
		            
		        } catch (Exception e) {
		            System.out.println("Image not found: " + e.getMessage());
		        }
		}
	    
	    wallWeaponsList.get(laneNumber).getChildren().add(imageView);
	}
	
	public static void addTitan(int laneNumber, Titan t) {
			VBox titanArea = new VBox();
			
			ProgressBar titanHealth = new ProgressBar(1.0);
			titanHealth.setPrefSize(25, 10);
			titanHealthBars.put(t,titanHealth);
			
	        Circle titanFig = new Circle();
	        titanFig.setRadius(10);
	        titanFig.setFill(Color.BLUE);
	        
	        titanArea.getChildren().addAll(titanHealth,titanFig);
	        double moveShiftX = control.getTitanSpawnDistance(); //lane lost when zero, spawns at 625
	        double moveShiftY = 55;
	     
	        titanArea.setTranslateX(moveShiftX);
	        titanArea.setTranslateY(moveShiftY);
	        allTitans.add(t);
	        titanFigs.put(t, titanArea);
	        lanePanes.get(laneNumber).getChildren().add(titanArea);
	}
	
	public static void removeTitan(Titan t) {
		allTitans.remove(t);
		titanFigs.get(t).getChildren().clear();
		System.out.println("REMOVE");
	}
	
	public static void moveTitan(Titan t) {
//		titanFigs.get(t).setTranslateX(t.getDistance());
		
		VBox titanArea = titanFigs.get(t);
		double targetX = t.getDistance();
		TranslateTransition translateTransition = new TranslateTransition();
		translateTransition.setDuration(Duration.millis(2000)); // 2 second for the animation (could be customized for each titan)
		translateTransition.setNode(titanArea);
		translateTransition.setToX(targetX);
		translateTransition.play();
	}
	
	public static void addTurnTitansToLanes() {
		 ArrayList<Lane> lanes = control.getOriginalLanes();
		 
		for(int i=0; i<lanes.size(); i++) {
			Lane l = lanes.get(i);
				if(!l.isLaneLost()) {
					PriorityQueue<Titan> laneTitans = l.getTitans();
					PriorityQueue<Titan> titans = new PriorityQueue<Titan>();
			
					for(Titan t: laneTitans)
						titans.add(t);
			
					for(Titan titan: titans) {
						
						if(titanFigs.containsKey(titan))
							moveTitan(titan);
						else
							addTitan(i,titan);
		             
						}
					}
				}
//		  for(Titan t: allTitans) {
//			if(t.isDefeated()) {
//				removeTitan(t);
//				System.out.println("7asal");
//				
//			}
//		}
	}
	
	public static void makeRadioButtons() {
		
		for(int i = 0; i<control.getLanes().size(); i++) {
			RadioButton radioButton = new RadioButton();
			radioButtons.add(radioButton);
			radioButton.setToggleGroup(toggleGrp);
			Lane lane = control.getOriginalLanes().get(i);
			buttonLaneHash.put(radioButton,lane);
			
			radioButton.setTranslateX(625);
		    radioButton.setTranslateY(37);
		    lanePanes.get(i).getChildren().add(radioButton);
		}
	}
	
	public static void checkLostLane() {
		
		for(Lane l: control.getOriginalLanes()) {
			if(l.getLaneWall().isDefeated()) {
				Rectangle rectangle = new Rectangle(650,90);
				rectangle.setFill(Color.GRAY);
				rectangle.setStroke(Color.GRAY);
				lanePanesHash.get(l).getChildren().add(rectangle);
				
				System.out.println("okk");
			}
				
		}
		
	}
	
	public static void buyWeapon(int code, int laneNumber) {
	    try {
	    	control.purchaseWeapon(code, control.getOriginalLanes().get(laneNumber));
	    	addWeapon(code,laneNumber);
	        updateScores();
	    } catch (InsufficientResourcesException e) {
	      
	    	Alert alert = new Alert(AlertType.ERROR);
		    alert.setTitle("Insufficient Resources");
		    alert.setContentText("You do not have enough resources to purchase this weapon.");
		    alert.showAndWait();
	       
	    } catch (InvalidLaneException e) {
	        
	    	Alert alert = new Alert(AlertType.ERROR);
		    alert.setTitle("Invalid Lane");
		    alert.setContentText("The selected lane is invalid.");
		    alert.showAndWait();
	       
	    }
	}
	
	public static void updateAll() {
		updateScores();
		updateWallProgBars();
		updateTitansProgBars();
		updateDangerLevel();
		
	}
	
	public static void passTurn() {
		
		if(control.isGameOver()) {
			
		}
		control.passTurn();
		addTurnTitansToLanes();
		updateAll();
		
		for(Titan t: allTitans) {
			if(t.isDefeated()) {
				removeTitan(t);
				System.out.println("7asal");
				
			}
		}
		//HashMap<Lane, Pane> lanePanesHash
//		for(Lane l: control.getOriginalLanes()) {
//			if(l.isLaneLost())
//				lanePanesHash.get(l).getChildren().get()
//		}
//		
	}
	

	public GUI() throws IOException{
		control = new Battle(0,0,0,0,0);
	}
    public static void main(String[] args) {
 
        launch(args);
        
    }

    

@Override
public void start(Stage primaryStage) throws IOException  {
	
	StackPane stackP = new StackPane();
	Label gameName = new Label("UTOPIA");
	gameName.setTranslateY(-140);
	gameName.setFont(Font.font("Arial", FontWeight.BOLD, 130));
	gameName.setTextFill(Color.DARKRED);

	
	CheckBox easy = new CheckBox("EASY");
	easy.setFont(Font.font("Arial", FontWeight.BOLD, 15));
	easy.setTranslateX(-80);
	easy.setSelected(false);
	
	CheckBox hard = new CheckBox("HARD");
	hard.setFont(Font.font("Arial", FontWeight.BOLD, 15));
	hard.setTranslateX(80);
	hard.setSelected(false);
	
	
	Button start = new Button("START");
	start.setFont(Font.font("Arial", FontWeight.BOLD, 15));
	start.setTranslateY(80);
	

	
	
	//---- WeaponShop: 
		// Weapon 1 Information
		VBox weaponInfo1 = new VBox();
		weaponInfo1.setSpacing(5);
		weaponInfo1.setPadding(new Insets(5));
		
		Image image = new Image(getClass().getResourceAsStream("/game/gui/volley.png"));
		ImageView weapon1ImageView = new ImageView(image);
	    weapon1ImageView.setFitHeight(30); // Adjust the size as needed
	    weapon1ImageView.setFitWidth(30);


	    HBox titleAndImage = new HBox(10); 
	    Label titleLabel = new Label("Anti-Titan Shell");
	    titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
	    titleAndImage.getChildren().addAll(weapon1ImageView, titleLabel);

	  
	    VBox weaponInfo11 = new VBox();
	    weaponInfo11.getChildren().add(titleAndImage);

	   
	    Label typeLabel = new Label("Type: Piercing Cannon");
	    typeLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
	    Label priceLabel = new Label("Price: 25");
	    priceLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
	    Label damageLabel = new Label("Damage: 10");
	    damageLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));

	    weaponInfo11.getChildren().addAll(typeLabel, priceLabel, damageLabel);
	    
		VBox weaponInfo2 = new VBox();
		Label name2 = new Label("Long Range Spear\n");
		Label type2 = new Label("Type: " + "Sniper Cannon");
		Label price2 = new Label("Price: " + 25);
		Label damage2 = new Label("Damage: " + 35);
		name2.setFont(Font.font("Arial", FontWeight.BOLD, 10));
		type2.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
		price2.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
		damage2.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
		weaponInfo2.getChildren().addAll(name2,type2,price2,damage2);
		weaponInfo2.setSpacing(5);
		weaponInfo2.setPadding(new Insets(5));
		
		VBox weaponInfo3 = new VBox();
		Label name3 = new Label("Wall Spread Cannon\n");
		Label type3 = new Label("Type: " + "VolleySpread Cannon");
		Label price3 = new Label("Price: " + 100);
		Label damage3 = new Label("Damage: " + 5);
		name3.setFont(Font.font("Arial", FontWeight.BOLD, 10));
		type3.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
		price3.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
		damage3.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
		weaponInfo3.getChildren().addAll(name3,type3,price3,damage3);
		weaponInfo3.setSpacing(5);
		weaponInfo3.setPadding(new Insets(5));
		
		VBox weaponInfo4 = new VBox();
		Label name4 = new Label("Proximity Trap\n");
		Label type4 = new Label("Type: " + "Wall Trap");
		Label price4 = new Label("Price: " + 75);
		Label damage4 = new Label("Damage: " + 100);
		name4.setFont(Font.font("Arial", FontWeight.BOLD, 10));
		type4.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
		price4.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
		damage4.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
		weaponInfo4.getChildren().addAll(name4,type4,price4,damage4);
		weaponInfo4.setSpacing(3);
		weaponInfo4.setPadding(new Insets(5));
		
		
		
		Button weapon1 = new Button();
		weapon1.setGraphic(weaponInfo11);
		Button weapon2 = new Button();
		weapon2.setGraphic(weaponInfo2);
		Button weapon3 = new Button();
		weapon3.setGraphic(weaponInfo3);
		Button weapon4 = new Button(); 
		weapon4.setGraphic(weaponInfo4);
		
		GridPane weaponShopGrid = new GridPane();
		weaponShopGrid.add(weapon1, 0, 0);
		weaponShopGrid.add(weapon2,0,1);
		weaponShopGrid.add(weapon3, 1,0);
		weaponShopGrid.add(weapon4, 1, 1);
		weaponShopGrid.setPadding(new Insets(20));;
		weaponShopGrid.setVgap(20);    
		weaponShopGrid.setHgap(20);
		weaponShopGrid.setGridLinesVisible(false);
		//---
	
	
	//----Deploy Weapons & Pass Turn Buttons:
	Button deployBut = new Button("DEPLOY WEAPON");
	deployBut.setFont(Font.font("Arial", FontWeight.BOLD, 15));
	
	Button passTurnBut = new Button("PASS TURN");
	passTurnBut.setFont(Font.font("Arial", FontWeight.BOLD, 15));
	passTurnBut.setTranslateX(20);
	
	VBox buttons = new VBox();
	buttons.getChildren().addAll(deployBut,passTurnBut);
	buttons.setTranslateX(70);
	buttons.setSpacing(20);
	buttons.setPadding(new Insets(20));
	//---------
	Group inst = new Group();		
	
	// description 
	
	Label Game_Instructions = new Label("Game Instructions: Attack on Titan: Utopia");
	Label introduction = new Label("Introduction :");
	Label Introduction = new Label("Welcome to Attack on Titan: Utopia, a tower defense game inspired by the hit anime Attack on Titan. In this game, you'll take on the role of a commander defending the Utopia District against waves of titan attacks. Your goal is to survive as long as possible and defeat as many titans \nas you can."); 
	Label spaceSetting= new Label("Space Setting:");
	Label SpaceSetting = new Label("The battlefield is divided into multiple lanes, each with a part of the wall to be defended. Your task is to deploy weapons strategically to defend these walls from incoming titan attacks.");
	Label EnemyCharacters= new Label("Enemy Characteres:");
	Label enemyCharacters= new Label("There are several types of titans, each with different attributes and behaviors. Titans will move closer to the walls each turn and attack if \nthey reach them. Defeat titans to earn resources and increase your score.");
	Label FriendlyPieces = new Label("Friendly Pieces:");
	Label friendlyPieces = new Label("You have access to various types of weapons to deploy against the titans. Each weapon has unique stats and attack actions. Choose your weapons wisely to maximize their effectiveness against different titan types.");
	Label GameRules = new Label("Game Rules:");
	Label gameRules = new Label("The game has no winning condition. Keep playing to defeat as many enemies as possible. Titans and weapons perform attack actions each turn. Defend the walls and defeat titans to survive. Lost lanes cannot have weapons deployed to them and will not spawn any more titans. Refill approaching titans each turn according to the current battle phase.");
	Label GameFlow = new Label("Game Flow");
	Label gameFlow = new Label("Each turn, choose to purchase and deploy a weapon or pass your turn. After your action, titans will move and attack, followed by weapons performing their attack actions. New titans are added to the lanes, and the battle phase may change based on the number of turns elapsed. Objective: Survive as long as possible and defeat as many titans as you can to increase your score. The game ends when all starting lanes\n become lost lanes.");
	Label goodLuck = new Label("Good luck, Commander! ");
	
	// the font 
	Game_Instructions.setFont(Font.font("Georgia", FontWeight.BOLD, 30));
	introduction.setFont(Font.font("Georgia",FontWeight.BOLD,20));
	Introduction.setFont(Font.font("Arial",FontWeight.NORMAL,15));
	spaceSetting.setFont(Font.font("Georgia",FontWeight.BOLD,20));
	SpaceSetting.setFont(Font.font("Arial",FontWeight.NORMAL,15));
	EnemyCharacters.setFont(Font.font("Georgia",FontWeight.BOLD,20));
	enemyCharacters.setFont(Font.font("Arial",FontWeight.NORMAL,15));
	FriendlyPieces.setFont(Font.font("Georgia",FontWeight.BOLD,20));
	friendlyPieces.setFont(Font.font("Arial",FontWeight.NORMAL,15));
	GameRules.setFont(Font.font("Georgia",FontWeight.BOLD ,20));
	gameRules.setFont(Font.font("Arial",FontWeight.NORMAL,15));
	GameFlow.setFont(Font.font("Georgia",FontWeight.BOLD,20));
	gameFlow.setFont(Font.font("Arial",FontWeight.NORMAL,15));
	goodLuck.setFont(Font.font("Georgia",FontWeight.BOLD,20));
	
	//the color 
	
	Game_Instructions.setTextFill(Color.DARKRED);
	introduction.setTextFill(Color.DARKRED);
	Introduction.setTextFill(Color.DARKSLATEGREY);
	spaceSetting.setTextFill(Color.DARKRED);
	SpaceSetting.setTextFill(Color.DARKSLATEGREY);
	EnemyCharacters.setTextFill(Color.DARKRED);
	enemyCharacters.setTextFill(Color.DARKSLATEGREY);
	FriendlyPieces.setTextFill(Color.DARKRED);
	friendlyPieces.setTextFill(Color.DARKSLATEGREY);
	GameRules.setTextFill(Color.DARKRED);
	gameRules.setTextFill(Color.DARKSLATEGREY);
	GameFlow.setTextFill(Color.DARKRED);
	gameFlow.setTextFill(Color.DARKSLATEGREY);
	goodLuck.setTextFill(Color.DARKRED);
	
	
	////space between the lines 
	
	Game_Instructions.setTranslateX(140);
	introduction.setTranslateX(70); //15
	introduction.setTranslateY(50);
	Introduction.setTranslateY(80);
	Introduction.setTranslateX(15);
	Introduction.setMaxWidth(980);
	Introduction.setWrapText(true);
	spaceSetting.setTranslateX(70);
	spaceSetting.setTranslateY(140);
	SpaceSetting.setTranslateX(15);
	SpaceSetting.setTranslateY(170);
	SpaceSetting.setMaxWidth(980);
	SpaceSetting.setWrapText(true);
	EnemyCharacters.setTranslateX(70);
	EnemyCharacters.setTranslateY(210);
	enemyCharacters.setTranslateX(15);
	enemyCharacters.setTranslateY(237);
	enemyCharacters.setMaxWidth(980);
	enemyCharacters.setWrapText(true);
	FriendlyPieces.setTranslateX(70);
	FriendlyPieces.setTranslateY(285);
	friendlyPieces.setTranslateX(15);
	friendlyPieces.setTranslateY(310);
	friendlyPieces.setMaxWidth(980);
	friendlyPieces.setWrapText(true);
	GameRules.setTranslateX(70);
	GameRules.setTranslateY(355);
	gameRules.setTranslateX(15);
	gameRules.setTranslateY(380);
	gameRules.setMaxWidth(980);
	gameRules.setWrapText(true);
	GameFlow.setTranslateX(70);
	GameFlow.setTranslateY(440);
	gameFlow.setTranslateX(15);
	gameFlow.setMaxWidth(980);
	gameFlow.setWrapText(true);
	gameFlow.setTranslateY(465);
	goodLuck.setTranslateX(70);
	goodLuck.setPadding( new Insets(20));
	goodLuck.setTranslateY(530);

	Button next = new Button("Next");
	next.setPadding( new Insets(20));
	next.setPrefHeight(5);
	next.setPrefWidth(75);
	next.setTranslateX(900);
	next.setTranslateY(530);
	
	// adding the labels to the group
	inst.getChildren().addAll(Game_Instructions,introduction,Introduction,spaceSetting,SpaceSetting,EnemyCharacters,enemyCharacters,FriendlyPieces,friendlyPieces,GameRules,gameRules,GameFlow,gameFlow,goodLuck,next);

	
	
	
	////--- Start Page:
	BackgroundFill bf = new BackgroundFill(Color.INDIANRED,null,null);
	Background bg = new Background(bf);
	stackP.setBackground(bg);
	stackP.getChildren().addAll(gameName,easy,hard,start);
	//------
	
	
	Scene s = new Scene(stackP,1000,600);	
	primaryStage.setScene(s);
	primaryStage.setTitle("Utopia");
	primaryStage.show();
	
	easy.setOnAction(new EventHandler <ActionEvent>(){
		@Override
		public void handle(ActionEvent event) {
			hard.setSelected(false);
			
				try {
					control = new Battle(0,0,580,3,250);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
		
		}
		});
	
	hard.setOnAction(new EventHandler <ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			easy.setSelected(false);
			try {
				control = new Battle(0,0,580,5,125);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		});

	weapon1.setOnAction(new EventHandler <ActionEvent>(){
		@Override
		public void handle(ActionEvent event) {
			if(toggleGrp.getSelectedToggle() == null) {
				Alert alert = new Alert(AlertType.ERROR);
			    alert.setTitle("Error");
			    alert.setContentText("Please choose a lane");
			    alert.showAndWait();
			}
			if(toggleGrp.getSelectedToggle() == radioButtons.get(0)) {
				buyWeapon(1,0);
				return;
			}
			if(toggleGrp.getSelectedToggle() == radioButtons.get(1)) {
				buyWeapon(1,1);
				return;
			}
			if(toggleGrp.getSelectedToggle() == radioButtons.get(2)) {
				buyWeapon(1,2);
				return;
			}
			if(radioButtons.get(3)!= null && toggleGrp.getSelectedToggle() == radioButtons.get(3)) {
				buyWeapon(1,3);
			}
			if(radioButtons.get(4)!= null && toggleGrp.getSelectedToggle() == radioButtons.get(4)) {
				buyWeapon(1,4);
			}
		}
		});	
	
	weapon2.setOnAction(new EventHandler <ActionEvent>(){
		@Override
		public void handle(ActionEvent event) {
			if(toggleGrp.getSelectedToggle() == null) {
				Alert alert = new Alert(AlertType.ERROR);
			    alert.setTitle("Error");
			    alert.setContentText("Please choose a lane");
			    alert.showAndWait();
			}
			if(toggleGrp.getSelectedToggle() == radioButtons.get(0)) {
				buyWeapon(2,0);
				return;
			}
			if(toggleGrp.getSelectedToggle() == radioButtons.get(1)) {
				buyWeapon(2,1);
				return;
			}
			if(toggleGrp.getSelectedToggle() == radioButtons.get(2)) {
				buyWeapon(2,2);
				return;
			}
			if(radioButtons.get(3)!= null && toggleGrp.getSelectedToggle() == radioButtons.get(3)) {
				buyWeapon(2,3);
			}
			if(radioButtons.get(4)!= null && toggleGrp.getSelectedToggle() == radioButtons.get(4)) {
				buyWeapon(2,4);
			}
		}
		});	
	
	weapon3.setOnAction(new EventHandler <ActionEvent>(){
		@Override
		public void handle(ActionEvent event) {
			if(toggleGrp.getSelectedToggle() == null) {
				Alert alert = new Alert(AlertType.ERROR);
			    alert.setTitle("Error");
			    alert.setContentText("Please choose a lane");
			    alert.showAndWait();
			}
			if(toggleGrp.getSelectedToggle() == radioButtons.get(0)) {
				buyWeapon(3,0);
				return;
			}
			if(toggleGrp.getSelectedToggle() == radioButtons.get(1)) {
				buyWeapon(3,1);
				return;
				
			}
			if(toggleGrp.getSelectedToggle() == radioButtons.get(2)) {
				buyWeapon(3,2);
				return;
			}
			if(radioButtons.get(3)!= null && toggleGrp.getSelectedToggle() == radioButtons.get(3)) {
				buyWeapon(3,3);
			}
			if(radioButtons.get(4)!= null && toggleGrp.getSelectedToggle() == radioButtons.get(4)) {
				buyWeapon(3,4);
			}
		}
		});	

	weapon4.setOnAction(new EventHandler <ActionEvent>(){
		@Override
		public void handle(ActionEvent event) {
			if(toggleGrp.getSelectedToggle() == null) {
				Alert alert = new Alert(AlertType.ERROR);
			    alert.setTitle("Error");
			    alert.setContentText("Please choose a lane");
			    alert.showAndWait();
			}
			if(toggleGrp.getSelectedToggle() == radioButtons.get(0)) {
				buyWeapon(4,0);
				return;
			}
			if(toggleGrp.getSelectedToggle() == radioButtons.get(1)) {
				buyWeapon(4,1);
				return;
			}
			if(toggleGrp.getSelectedToggle() == radioButtons.get(2)) {
				buyWeapon(4,2);
				return;
			}
			
			if(radioButtons.get(3)!= null && toggleGrp.getSelectedToggle() == radioButtons.get(3)) 
				buyWeapon(4,3);
			
			if(radioButtons.get(4)!= null && toggleGrp.getSelectedToggle() == radioButtons.get(4)) 
				buyWeapon(4,4);
			
		}
		});	
	
	passTurnBut.setOnAction(new EventHandler <ActionEvent>(){
		@Override
		public void handle(ActionEvent event) {
			passTurn();
		}
		});	
		
	next.setOnAction(new EventHandler <ActionEvent>(){
		@Override
		public void handle(ActionEvent event) {
			updateScores();
			VBox left = new VBox(); 
		    left.getChildren().addAll(scoresList,constructLanes());
		    VBox right = new VBox();
		    right.getChildren().addAll(weaponShopGrid,buttons);
	
		   
	    
		    GridPane mainGrid = new GridPane();
		    mainGrid.add(left,0,0);
		    mainGrid.add(right,1,0);
			mainGrid.setGridLinesVisible(true);
			
			Scene game = new Scene(mainGrid,1200,700);
	    	primaryStage.setScene(game);
	    	primaryStage.setTitle("Utopia");
	    	primaryStage.show();
		}
		});	
	
	start.setOnAction(new EventHandler <ActionEvent>(){
		@Override
		public void handle(ActionEvent event) {
			Scene instructions = new Scene(inst,1000,600);
			primaryStage.setScene(instructions);
			primaryStage.setTitle("Utopia");
			primaryStage.show();
		}
		});	

}}




    






    //---- WALL WEAPONS VBOX: 
//    VBox wallWeapons = new VBox();
//    Circle red = new Circle(20,Color.RED);
//    Circle blue = new Circle(20,Color.BLUE);
//    Circle yellow = new Circle(20,Color.YELLOW);
//    wallWeapons.getChildren().addAll(red,blue,yellow);
    //---


