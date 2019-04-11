package rpc;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.BearerToken;
import oAuth.CreateAndVerify;

/**
 * Servlet implementation class CancelOrder
 */
@WebServlet("/cancelorder")
public class CancelOrder extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CancelOrder() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String token = BearerToken.getBearerToken(request);
		JSONObject obj = new JSONObject();
		
		if(token != null && CreateAndVerify.isTokenValid(token, request.getRemoteAddr())) {
			// connect to database
			DBConnection conn = DBConnectionFactory.getConnection();
			// acquire parameters from front end
			JSONObject input = RpcHelper.readJSONObject(request);
			String orderId = input.getString("order_id").toString();
			if(orderId != null && conn.validateOrderId(orderId)) {
				response.setStatus(200);
				if(conn.cancelOrder(orderId)){
					obj.put("status", "you have successfully deleted order" + orderId);
				}else {
					obj.put("status", "something went wrong, your request to cancel order " + orderId + " has failed");
				}
			}else {
			    response.setStatus(400);
				obj.put("status", "the provided orderId does not exist");
			}		
			
			RpcHelper.writeJsonObject(response, obj);	
			
		} else {
			obj.put("status", "are you trying to gain illegal access? Where is your token?");
			response.setStatus(400);
			RpcHelper.writeJsonObject(response, obj);
		}
		
	}

}
