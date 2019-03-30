## Project Motivation:
* Inspired by drone delivery projects such as Google's X-wing and Amazon's Prime Air,
  our company seeks to provide a robot delivery service in San Francisco.

## Impact:
* We have three bases located in San Francisco, each covers a different area, aiming to reach most neighborhoods.
* We will make local short distance delivery available in a convenient and cheap fashion, while not adding any more traffic to the roads.

## Basic Architecture:
* #### Authentication
  * ##### Log in
    > verify username & password -> create token -> return token to front end
  * ##### Log out
    > add user's token to blacklist -> return user to login page
  * ##### Register
    > check if token exists -> if token does not exist -> let user register -> else tell user to log out first before able to register

* #### Order
  * ##### Get recommended delivery option
    > user input pick-up location and destination -> find closest bases with available ground bot and drone -> calculate time, distance, and cost for both robots to travel from base to pick-up location and to destination
      -> return to user with two delivery options, the time and money costs for drone and those for ground bot
  * ##### Submit order
    > if user choses one of the two options returned above, user inputs a few more information such as the name of sender and that of receiver,
      -> submits Order -> a new order is created -> the selected type of robot will be located at the closest available base and assigned to execute this order,
      -> user will be notified if order is successfully created and robot is on its way to pick up package
  * ##### Cancel Order
    > by giving an orderId, user can cancel a current order only if package has not yet been picked up by robot,
      -> the status of this order will be set to "canceled", and moved from currentOrder to historyOrder,
      -> the assigned robot will be released of duty and return to closest base
  * ##### Track order
    > user can provide an orderId to get all the information on the corresponding order,
      such as its current location and progress status such as "being retrieved" or "being delivered"
  * ##### Confirm Order
    > once robot reached its destination, user can confirm the order -> it will set the order status to be "completed",
      and this order will be moved from currentOrder to historyOrder -> the assigned robot will return to closest base
  * ##### Get all current orders
    > user can browse all of their current orders, current as in any ongoing order that is not yet confirmed to be completed or canceled
  * ##### Get all history orders
    > user can browse all of their past orders, past as in any order with status "completed" or "canceled"
