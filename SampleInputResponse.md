a documentation of sample input and response for each endpoint. still under construction.

#### `[POST] /login `
* ##### Input
```JSON
{
    "username": "neko",
    "password": "neko"
}
```
* ##### Response
  `success`
```JSON
{
    "access_token": "Dha44o9fiW5Ka..."
}
```
 `fail`
```JSON
{
   "": ""
}
```

#### `[GET] /logout`
* ##### Input
`Header: Your token`
* ##### Response
  `success`
```JSON
{
    "status": ""
}
```
 `fail`
```JSON
{
    "status": ""
}
```

#### `[POST] /register`
* ##### Input
```JSON
{
    "username": "pipiP",
    "password": "popko",
    "email": "popteamepic@email.com",
    "first_name": "P",
    "last_name": "pipi"
}
```
* ##### Response
  `success`
```JSON
{
    "status": "you have wooooooo offically joined the rainbowBunny club, welcome!"
}
```
  `fail`
  ```JSON
  {
    "": ""
  }
  ```


  #### `[GET] /login (this end point is provided for testing whether a token is still valid)`
  * ##### Input
  `Header: Your token`
  * ##### Response
    `success`
  ```JSON
  {
      "wooo": "your token is still valid"
  }
  ```
   `fail`
  ```JSON
  {
      "status": ""
  }
  ```


  #### `[GET] /searchroute`
  * ##### Input
  ```JSON
  {
      "origin": "3369 Mission St, San Francisco, CA 94110",
      "destination": "448 Cortland Ave, San Francisco, CA 94110"
  }
  ```
  * ##### Response
    `success`
  ```JSON
  {
      "DeliveryAddress": {
        "destiantion": "448+Cortland+Ave,+San+Francisco,+CA+94110",
        "origin": "3369+Mission+St,+San+Francisco,+CA+94110"
      },
      "GroundBot": {
        "cost": 0.9929511651952596,
        "pickup_time": 2,
        "travel_time": 5,
        "travel_distance": 0.9929511651952596,
        "avail_status": "yes",
        "base": "448 Cortland Ave, San Francisco, CA 94110"
      },
      "Drone": {
        "cost": 5.915172631323721,
        "pickup_time": 4.2859713212489465,
        "travel_time": 0.4461667838100305,
        "travel_distance": 3.943448420882481,
        "avail_status": "yes",
        "base": "75 Geary Blvd, San Francisco, CA 94118"
      }
}
  ```
   `fail`
  ```JSON
  {
      "status": "are you trying to gain illegal access? Where is your token?"
  }
  ```


  #### `[POST] /submitorder`
  * ##### Input
  ```JSON
  {
  	"origin" : "123 Sunset Blvd, Santa Monica, CA 12345",
  	"destination" : "456 Beach, Santa Monica, CA 67890",
  	"travel_time" : "20",
  	"cost" : "10",
  	"sender" : "pipiP",
  	"receiver" : "popko",
  	"address" : "75 Geary Blvd, San Francisco, CA 94118",
  	"type" : "drone"
  }
  ```
  * ##### Response
  `success`
  ```JSON
  {
      "robot_status": "assigned robot has received your order and is now on its way to retrieve the package",
      "status": "your order has been created"
  }
  ```
  `fail: invalid token`
  ```JSON
  {
    "status": "are you trying to gain illegal access? Where is your token?"
  }
  ```

#### `[GET] /currentorder`
  * ##### Input
    only need token in Header
  * ##### Response
      `success`
    ```JSON
    {
      "robot_status": "assigned robot has received your order and is now on its way to retrieve the package",
      "status": "your order has been created"
    }
    ```
    `fail: invalid token`
    ```JSON
    {
      "status": "are you trying to gain illegal access? Where is your token?"
    }
    ```

#### `[GET] /orderhistory`
  * ##### Input
    only need token in Header
  * ##### Response
      `success`
    ```JSON
    [
        {
          "a_arrival": "12:00pm 3/17/2019",
          "cost": "0.75",
          "receiver": "The Good Life Grocery",
          "create_time": "20190321105623",
          "origin": "3369 Mission St, San Francisco, CA 94110",
          "destination": "448 Cortland Ave, San Francisco, CA 94110",
          "orderStatus": "0",
          "e_arrival": "11:30am 3/17/2019",
          "sender": "hitobito",
          "user_id": "hito2019032704500200004",
          "robot_id": "2",
          "robotType": "drone",
          "order_id": "52019032705031200010"
        }
    ]
    ```
    `fail: invalid token`
    ```JSON
    {
      "status": "are you trying to gain illegal access? Where is your token?"
    }
    ```

#### `[GET] /trackorder`
  * ##### Input
    ```JSON
    {
     "order_id": "0201903275"
    }
    ```
  * ##### Response
      `success`
    ```JSON
    [
        {
          "cost": "0.6",
          "e_arrival": "5:00pm 3/25/2019",
          "receiver": "Four Barrel Coffee",
          "create_time": "20190305112325",
          "sender": "koneko",
          "robot_id": "4",
          "robotType": "ground",
          "origin": "3639 18th St, San Francisco, CA 94110",
          "destination": "375 Valencia St, San Francisco, CA 94103",
          "order_id": "02019032705031200005",
          "currentLocation": "3639 18th St, San Francisco, CA 94110"
        }
    ]
    ```
      `fail: cannot find any match for the provided orderId`
      ```JSON
    [
        {
          "isEmpty": "cannot find any order that matches your orderId"
        }
    ]
      ```

#### `[GET] /confirmorder`
* ##### input
```JSON
{
    "order_id" : "2019033002523200002"
}
```
* ##### Response
`success`
```JSON
{
  "status" : "you have successfully deleted order <order_id>"
}
```
`fail: input orderId does not exist`
```JSON
{
    "status": "the provided orderId does not exist"
}
```
`fail: error in sql execution during process of confirming order`
```JSON
{
  "status" : "something went wrong, your request to cancel order <order_id> has failed"
}
```

#### `[GET] /cancelorder`
* ##### input
```JSON
{
 "order_id" : "2019033003003600000"
}
```
* ##### Response
`success`
```JSON
{
    "status": "you have successfully deleted order2019033003003600000"
}
```
`fail: input orderId does not exist`
```JSON
{
    "status": "the provided orderId does not exist"
}
```
`fail: error in sql execution during process of confirming order`
```JSON
{
  "status" : "something went wrong, your request to cancel order <order_id> has failed"
}
```
