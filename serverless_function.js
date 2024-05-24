const mysql = require('mysql');

const pool = mysql.createPool({
    connectionLimit: 10,
    host: '34.141.109.109',
    user: 'root',
    password: 'Sabanci@123',
    database: 'bank'
});

exports.myDatabaseFunction = (req, res) => {
    const id = req.query.id; // Assuming you want to filter or just show all if no ID

    let query = 'SELECT * FROM transaction';
    let queryParams = [];

    if (id) {
        query += ' WHERE receiver_id = ?';
        queryParams.push(id);
    }

    pool.query(query, queryParams, (error, transactions, fields) => {
        if (error) {
            res.status(500).send(error.message);
            return;
        }

        if (transactions.length > 0) {
            // Calculate the average amount
            const totalAmount = transactions.reduce((acc, transaction) => acc + transaction.amount, 0);
            const averageAmount = totalAmount / transactions.length;

            // Send the average along with the transactions
            res.json({
                averageAmount: averageAmount,
                transactions: transactions
            });
        } else {
            res.status(404).send('No records found');
        }
    });
};
