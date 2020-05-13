curl -i -XPOST http://localhost:9999/api/main/bids/contact-info-verify -H "Content-Type: application/json" -d \
'
{
	"city": "Москва",
	"partner": "KUB",
	"phone": "+79201162631",
	"userId": ""
}
'
