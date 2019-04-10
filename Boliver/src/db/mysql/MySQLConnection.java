package db.mysql;

import java.sql.Connection;
import java.util.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import db.DBConnection;
import entity.BaseStatus;
import entity.BaseStatus.BaseStatusBuilder;
import entity.Order;
import entity.Order.OrderBuilder;
import recommendation.ClosestBaseToRobot;

public class MySQLConnection implements DBConnection {
	private Connection conn;

	public MySQLConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean verifyLogin(String username, String password) {
		if (conn == null) {
			return false;
		}
		try {
			String sql = "SELECT user_id FROM Users WHERE username = ? AND pwd = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, username);
			statement.setString(2, password);

			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return false;
	}

	@Override
	public boolean isBlackListed(String token) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		try { 
			
			String sql = "SELECT token FROM BlackList WHERE token = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, token);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public boolean addToBlackList(String token) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		try {
			String sql = "INSERT IGNORE INTO BlackList VALUES(?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, token);
			return ps.executeUpdate() == 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public boolean registerUser(String userId, String username, String password, String email, String firstname,
			String lastname) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		try {
			String sql = "INSERT IGNORE INTO Users VALUES(?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ps.setString(2, username);
			ps.setString(3, password);
			ps.setString(4, email);
			ps.setString(5, firstname);
			ps.setString(6, lastname);

			return ps.executeUpdate() == 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public String getDroneSpeed(String type) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return null;
		}
		try {
			String sql = "SELECT speed FROM RobotType WHERE type = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, type);

			ResultSet rs = ps.executeQuery();
			String[] speed = new String[1];
			while(rs.next()) {
				speed[0] = rs.getString("speed");
				// for debug: System.out.println("speeeeeeeeed: " + speed);
			}
			return speed[0];
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public String getUserId(String username) {
		if (conn == null) {
			System.out.println("DB connection failed for getCurrentOrders getHistoryOrders");
			return null;
		}
		try {
			String sql = "SELECT user_id FROM Users WHERE username = ?";
		    PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();
			String userId = null;
			while(rs.next()) {
				userId = rs.getString("user_id");
			}
			return userId;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public List<Order> getHistoryOrders(String userId, Integer start, Integer end) {
		
		if (conn == null) {
			System.out.println("DB connection failed for getCurrentOrders getHistoryOrders");
			return new ArrayList<>();
		}
		List<Order> historyOrders = new ArrayList<>();
		try {
			String sql = "SELECT a.user_id user_id, a.sender, a.receiver, a.order_id order_id,a.robot_id robot_id,a.order_status order_status,"
					     + "a.origin origin,a.destination destination,a.e_arrival e_arrival,a.a_arrival a_arrival,"
					     + "a.create_time create_time,a.cost cost,c.type type From OrderHistory a,Robot b,RobotType c"
					     + " where a.user_id = ? and a.robot_id=b.robot_id and b.type_id=c.type_id ORDER BY STR_TO_DATE(a_arrival,'%H:%i EDT %m-%d-%Y') DESC";
			
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);

			ResultSet rs = stmt.executeQuery();

			OrderBuilder builder = new OrderBuilder();

			while (rs.next()) {		
				builder.setUserId(rs.getString("user_id"));
				builder.setOrderId(rs.getString("order_id"));
				builder.setRobotId(rs.getString("robot_id"));
				builder.setOrderStatus(rs.getString("order_status"));
				builder.setOrigin(rs.getString("origin"));
				builder.setDestination(rs.getString("destination"));
				builder.setSender(rs.getString("sender"));
				builder.setReceiver(rs.getString("receiver"));
				builder.seteArrival(rs.getString("e_arrival"));
				builder.setaArrival(rs.getString("a_arrival"));
				builder.setCreateTime(rs.getString("create_time"));
				builder.setCost(rs.getString("cost"));
				builder.setRobotType(rs.getString("type"));
				historyOrders.add(builder.build());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return historyOrders;
	}
	
	@Override
	public List<Order> getCurrentOrders(String userId){
		if (conn == null) {
			System.out.println("DB connection failed for getCurrentOrders");
			return new ArrayList<>();
		}
		List<Order> CurrentOrders = new ArrayList<>();
		try {
			String sql = "SELECT CurrentOrder.order_id, CurrentOrder.robot_id, RobotType.type, CurrentOrder.order_status, CurrentOrder.sender, CurrentOrder.receiver, Robot.curLocation, CurrentOrder.origin, CurrentOrder.destination, CurrentOrder.e_arrival, CurrentOrder.create_time, CurrentOrder.cost   \r\n" + 
					"\r\n" + 
					"FROM CurrentOrder\r\n" + 
					"INNER JOIN Robot ON CurrentOrder.robot_id = Robot.robot_id \r\n" + 
					"INNER JOIN RobotType ON Robot.type_id = RobotType.type_id\r\n" + 
					"WHERE CurrentOrder.user_id = ? ORDER BY CurrentOrder.order_id DESC";
			
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);

			ResultSet rs = stmt.executeQuery();

			OrderBuilder builder = new OrderBuilder();

			while (rs.next()) {		
				builder.setOrderId(rs.getString("order_id"));
				builder.setRobotId(rs.getString("robot_id"));
				builder.setOrderStatus(rs.getString("order_status"));
				builder.setOrigin(rs.getString("origin"));
				builder.setDestination(rs.getString("destination"));
				builder.setSender(rs.getString("sender"));
				builder.setReceiver(rs.getString("receiver"));
				builder.seteArrival(rs.getString("e_arrival"));
				builder.setCreateTime(rs.getString("create_time"));
				builder.setCost(rs.getString("cost"));
				builder.setRobotType(rs.getString("type"));
				CurrentOrders.add(builder.build());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return CurrentOrders;
	}

	@Override
	public boolean placeOrder(Order order) {
		if (conn == null) {
			System.out.println("DB connection failed");
			return false;
		}
		try {
			String sql = "INSERT INTO CurrentOrder VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	   		PreparedStatement ps = conn.prepareStatement(sql);
	   		
	   		if (
	   				order.getOrderId() == null && 
	   				order.getRobotId() == null && 
	   				order.getUserId() == null && 
	   				order.getOrderStatus() == null && 
	   				order.getOrigin() == null && 
	   				order.getDestination() == null &&
					order.geteArrival() == null &&
					order.getCreateTime() == null &&
					order.getCost() == null &&
					order.getSender() == null &&
					order.getReceiver() == null
   				) {
	   			System.out.println("There is null in the data.");
	   			return false;
	   		}
	   		
	   		ps.setString(1, order.getOrderId());
	   		ps.setString(2, order.getRobotId());
	   		ps.setString(3, order.getUserId());
	   		ps.setString(4, order.getOrderStatus());
	   		ps.setString(5, order.getOrigin());
	   		ps.setString(6, order.getDestination());
	   		ps.setString(7, order.getSender());
	   		ps.setString(8, order.getReceiver());
	   		ps.setString(9, order.geteArrival());
	   		ps.setString(10, order.getCreateTime());
	   		ps.setString(11, order.getCost());
	   		
	   		//System.out.println(ps);
	   		
	   		return ps.executeUpdate() == 1;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	@Override
	public Set<Order> trackOrder(String orderId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return null;
		}
		Set<Order> results = new HashSet<>();
		try {
			String sql ="SELECT CurrentOrder.order_id, CurrentOrder.robot_id, CurrentOrder.sender, CurrentOrder.receiver, CurrentOrder.order_status, RobotType.type, Robot.curLocation, CurrentOrder.origin, CurrentOrder.destination, CurrentOrder.e_arrival, CurrentOrder.create_time, CurrentOrder.cost   \r\n" + 
					"\r\n" + 
					"FROM CurrentOrder\r\n" + 
					"INNER JOIN Robot ON CurrentOrder.robot_id = Robot.robot_id \r\n" + 
					"INNER JOIN RobotType ON Robot.type_id = RobotType.type_id\r\n" + 
					"WHERE order_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, orderId);

			ResultSet rs = stmt.executeQuery();

			OrderBuilder builder = new OrderBuilder();

			while (rs.next()) {		
				builder.setOrderId(rs.getString("CurrentOrder.order_id"));
				builder.setRobotId(rs.getString("CurrentOrder.robot_id"));
				builder.setRobotType(rs.getString("RobotType.type"));
				builder.setCurrentLocation(rs.getString("Robot.curLocation"));
				builder.setOrigin(rs.getString("CurrentOrder.origin"));
				builder.setDestination(rs.getString("CurrentOrder.destination"));
				builder.setSender(rs.getString("sender"));
				builder.setReceiver(rs.getString("receiver"));
				builder.seteArrival(rs.getString("CurrentOrder.e_arrival"));
				builder.setCreateTime(rs.getString("CurrentOrder.create_time"));
				builder.setCost(rs.getString("CurrentOrder.cost"));
				builder.setOrderStatus(rs.getString("CurrentOrder.order_status"));
				results.add(builder.build());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return results;
	}
	
	@Override
	public List<BaseStatus> getBaseStatus(){
		if (conn == null) {
			System.err.println("DB connection failed");
			return null;
		}
		
		List<BaseStatus> results = new ArrayList<>();
		
		try {
			for(int i = 1; i < 4; i++) {
				String sql = "SELECT type_id, lat, lon, address FROM `Robot` INNER JOIN Base ON Robot.base_id = Base.base_id WHERE Robot.base_id = ? GROUP BY type_id";
				PreparedStatement stmt = conn.prepareStatement(sql);
				stmt.setInt(1, i);
				ResultSet rs = stmt.executeQuery();

				while(rs.next()) {
					BaseStatusBuilder builder = new BaseStatusBuilder();
					if(rs.getString("type_id").equals("1")) {
						builder.setGround(true);
					}
					if(rs.getString("type_id").equals("2")) {
						builder.setDrone(true);
					}
					builder.setBaseId(i);
					builder.setLat(rs.getString("lat"));
					builder.setLon(rs.getString("lon"));
					builder.setAddress(rs.getString("address"));
					results.add(builder.build());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}
	
	@Override
	public String getRobotId(String baseAddress, String RobotType) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return null;
		}
		
		try {
			String sql = "SELECT robot_id FROM `Robot` "
					+ "INNER JOIN Base ON Robot.base_id = Base.base_id "
					+ "INNER JOIN RobotType ON Robot.type_id = RobotType.type_id "
					+ "WHERE address = ? AND RobotType.type = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, baseAddress);
			stmt.setString(2, RobotType);
			
			ResultSet rs = stmt.executeQuery();
			String robotId = null;
			while(rs.next()) {
				robotId = rs.getString("robot_id");
				return robotId;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public boolean updateRobotStatus(String robotId, String destination, String newStatus, String baseId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		try {
			String sql = "UPDATE Robot SET robotStatus = ?, destination= ?, base_id = ? WHERE robot_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, newStatus);
			stmt.setString(2, destination);
			stmt.setString(3, baseId);
			stmt.setString(4, robotId);
			
			return stmt.executeUpdate() == 1;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	@Override
	public boolean updateOrderStatus(String orderId, String newStatus) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		Calendar calendar = Calendar.getInstance();
		Date curTimeRaw = calendar.getTime();
		String pattern = "HH:mm zzz MM-dd-yyyy";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String curTime = simpleDateFormat.format(curTimeRaw);
		try {
			String sql = "UPDATE OrderHistory SET order_status = ?, a_arrival= ? WHERE order_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, newStatus);
			stmt.setString(2, curTime);
			stmt.setString(3, orderId);
			
			return stmt.executeUpdate() == 1;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	@Override
	public boolean validateOrderId(String orderId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		try {
			String sql = "SELECT * FROM CurrentOrder WHERE order_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, orderId);
			
			ResultSet rs = stmt.executeQuery();
			
			if(rs.next()) {
				return true;
			}
		} catch(Exception e) {
			System.out.println("validateOrderId() error");
			e.printStackTrace();
		}
		
		return false;
	}
	
	@Override
	public boolean cancelOrder(String orderId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		return moveOrder(orderId, "4");
	}
	
	@Override
	public boolean confirmOrder(String orderId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		return moveOrder(orderId, "3");
	}
	
	@Override
	public boolean moveOrder(String orderId, String newStatus) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
			
		// get robot_id, curLocation of Robot, given order_id
		String robotId = null;
		String curLocation = null;
		try {
			String sql = "SELECT CurrentOrder.robot_id, Robot.curLocation FROM CurrentOrder INNER JOIN Robot ON CurrentOrder.robot_id = Robot.robot_id WHERE order_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, orderId);
			
			
			ResultSet rsGetRobot = stmt.executeQuery();
			while(rsGetRobot.next()) {
				robotId = rsGetRobot.getString("robot_id");
				curLocation = rsGetRobot.getString("curLocation");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// calculate closest Base and get its address
		System.out.println("MySQLConnection.moveOrder.curLocation: " + curLocation);
		String returnBaseAddr = ClosestBaseToRobot.getAddress(curLocation);
		
		// update robotStatus
		boolean updateRobot = updateRobotStatus(robotId, returnBaseAddr, "returning", "-1"); // {robotId, returning Base address, new Robot status, new base_id status(status remains -1 as it has not yet arrived at any Base)}
		
		boolean deleteOrder = false;
		boolean moved = false;

		// copy paste order from CurrentOrder to orderHisotry
		try { // TODO
			String sql = "INSERT INTO OrderHistory (order_id, robot_id, user_id, order_status, origin, destination, sender, receiver, e_arrival, create_time, cost)\r\n" + 
					     "SELECT order_id, robot_id, user_id, order_status, origin, destination, sender, receiver, e_arrival, create_time, cost\r\n" + 
					     "FROM CurrentOrder \r\n" + 
					     "WHERE CurrentOrder.order_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, orderId);
			
			moved = stmt.executeUpdate() == 1;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// update orderStatus
		boolean updateOrder = updateOrderStatus(orderId, newStatus);
		
		// delete order from CurrentOrder
		try {
			String sql = "DELETE FROM CurrentOrder WHERE order_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, orderId);
			
			deleteOrder = stmt.executeUpdate() == 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// going to log each success or failure 
		if(updateRobot) {
			System.out.println("updateRobot is a success");

			if(moved) {
				System.out.println("moving order from current to history is a success");
				
				if(updateOrder) {
					System.out.println("updateOrder is a success");
					
					if(deleteOrder) {
						System.out.println("delete order from CurrentOrders is a success");
						return true;
						
					} else {
						System.out.println("delete order from CurrentOrders has failed");
					}
				} else {
					System.out.println("updating order has failed");
				}
			} else {
				System.out.println("moving order has failed");
			}
		} else {
			System.out.println("updateRobot has failed");
		}
		
		System.out.println("if you have reached here, this is the end of line before returning false of moveOrder( )");
		
		//return updateRobot && moved && updateOrder && deleteOrder;
		return false;
		
	}
}
