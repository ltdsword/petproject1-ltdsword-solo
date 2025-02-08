require("dotenv").config();
const express = require("express");
const cors = require("cors");
const bodyParser = require("body-parser");
const sgMail = require("@sendgrid/mail");

const app = express();
app.use(cors()); // Allows requests from your Android app
app.use(bodyParser.json());

sgMail.setApiKey(process.env.SENDGRID_API_KEY); // Load API key securely

// Endpoint to send a verification email
app.post("/send-email", async (req, res) => {
    const { email, verificationCode } = req.body;

    const msg = {
        to: email,
        from: "no-reply@ltdsword.com",
        subject: "Verify Your Email - YourApp",
        text: `Your verification code is: ${verificationCode}\n\nPlease enter this code to verify your email.`,
        html: `<h3>Hello,</h3>
               <p>Your verification code is:</p>
               <h2 style="color:blue;">${verificationCode}</h2>
               <p>Please enter this code in the app to verify your email.</p>
               <p>Best Regards,<br><strong>Le Tien Dat</strong>, Developer.</p>`,
    };

    try {
        await sgMail.send(msg);
        res.status(200).json({ success: true, message: "Email sent successfully!" });
    } catch (error) {
        console.error("Error sending email:", error);
        res.status(500).json({ success: false, message: "Failed to send email." });
    }
});

// Start the server
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));