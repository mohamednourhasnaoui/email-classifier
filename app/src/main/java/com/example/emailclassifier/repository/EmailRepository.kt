package com.example.emailclassifier.repository

import com.example.emailclassifier.data.Email

class EmailRepository {

    fun getEmails(): List<Email> {
        return listOf(
            Email(
                id = 1,
                sender = "promo@winner.com",
                subject = "Congratulations, you won a prize",
                body = "Click here now to claim your free reward and limited offer."
            ),
            Email(
                id = 2,
                sender = "manager@company.com",
                subject = "Project meeting tomorrow",
                body = "Please prepare the project report and send the presentation before the meeting."
            ),
            Email(
                id = 3,
                sender = "friend@email.com",
                subject = "Weekend plans",
                body = "Hey, how are you? Do you want to meet this weekend with the family?"
            ),
            Email(
                id = 4,
                sender = "security@bank-alert.com",
                subject = "Urgent account verification required",
                body = "Verify your bank account now to avoid suspension. Click the secure link immediately."
            ),
            Email(
                id = 5,
                sender = "hr@company.com",
                subject = "Training session reminder",
                body = "Reminder to attend the training session tomorrow. Please confirm your availability."
            ),
            Email(
                id = 6,
                sender = "sister@email.com",
                subject = "Family dinner",
                body = "Mom asked if you are coming to dinner tonight. Please call me when you arrive home."
            )
        )
    }
}