from locust import HttpUser, task, between


class WebsiteUser(HttpUser):

    host = "http://34.107.248.67:8888"

    wait_time = between(2, 7)  # Simulates a wait time between tasks of 1 to 5 seconds

    @task(1)
    def load_documentation(self):
        self.client.get("/api-docs")

    @task(1)  # This task is 3 times more likely to be picked
    def load_customers(self):
        self.client.get("/v1/customers/find_all")

    @task(3)  # This task is 3 times more likely to be picked
    def trigger_function(self):
            self.client.get("/v1/transactions/calculate_user_average_income/1")

    @task(2)  # This task is 3 times more likely to be picked
    def trigger_function(self):
        self.client.get("/v1/transactions/find_all_by_receiver/1")


    @task(10)
    def post_transaction(self):
        # Data to send in the POST request (as JSON)
        data = {
            "amount": 1.0,
            "senderId": 3,
            "receiverId": 1
        }
        headers = {'content-type': 'application/json'}

        # Sending the POST request to a specific endpoint
        self.client.post("/v1/transactions/save", json=data, headers=headers)
