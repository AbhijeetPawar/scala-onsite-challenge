package com.example

// id;title;budget;target_country_code;target_mobile_app;target_connection_type
// 1;CocaCola Life;50000;DE;com.rovio.angry_birds;WiFi

case class Campaign(id: Int, title: String, Budget: BigDecimal, countryCode: String, mobileApp: String, connectionType: String)
