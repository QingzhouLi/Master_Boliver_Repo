package rpc;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.BearerToken;
import entity.Order;
import oAuth.CreateAndVerify;


/**
 * Servlet implementation class OrderHistory
 */
@WebServlet("/orderhistory")
public class OrderHistory extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public OrderHistory() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("received a order history request");
		// Get token from request
		String token = BearerToken.getBearerToken(request);
		
		if(token != null && CreateAndVerify.isTokenValid(token, request.getRemoteAddr())) {
			DBConnection conn = DBConnectionFactory.getConnection();
			try {				
				String userId = CreateAndVerify.getUserId(token);
				JSONArray array = new JSONArray();
				List<Order> orders = conn.getHistoryOrders(userId, null, null);
				for (Order order : orders) {
					JSONObject obj = order.toJSONObject();
					array.put(obj);
				}
				
				RpcHelper.writeJsonArray(response, array);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				conn.close();
			}
		} else {  // if your token is invalid,
			JSONObject obj = new JSONObject();
			obj.put("status", "are you trying to gain illegal access? Where is your token?");
			RpcHelper.writeJsonObject(response, obj);
		}
		
	}

}
