from locust import HttpUser, task, between


class WebsiteUser(HttpUser):
    host = "http://34.117.141.149:8888"

    wait_time = between(1, 5)  # Simulates a wait time between tasks of 1 to 5 seconds

    @task
    def load_documentation(self):
        self.client.get("/api-docs")

    @task(3)  # This task is 3 times more likely to be picked
    def load_customers(self):
        self.client.get("/v1/customers/find_all")

    @task(10)
    def post_transaction(self):
        # Data to send in the POST request (as JSON)
        data = {
            "amount": 10000.0,
            "senderId": 10,
            "receiverId": 12
        }
        headers = {'content-type': 'application/json'}

        # Sending the POST request to a specific endpoint
        self.client.post("/v1/transactions/save", json=data, headers=headers)
