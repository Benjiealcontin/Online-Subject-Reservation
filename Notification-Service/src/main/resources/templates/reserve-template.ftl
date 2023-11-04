<!DOCTYPE html>
<html>
<head>
<title>Subject Reservation Confirmation</title>
<style>
body {
font-family: Arial, sans-serif;
background-color: #f4f4f4;
}
.container {
max-width: 600px;
margin: 0 auto;
padding: 20px;
background-color: #ffffff;
box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
h1 {
color: #333;
}
p {
color: #666;
}
</style>
</head>
<body>
<div class="container">
        <h1>Subject Reservation Confirmation</h1>
        <p>Dear ${familyName},</p>
        <p>Your reservation for the subject <strong>${subjectName}</strong> has been successfully submitted. You are now waiting for admin approval.</p>
        <p>Reservation Details:</p>
        <ul>
            <li><strong>Transaction ID:</strong> ${transactionId}</li>
            <li><strong>Subject Code:</strong> ${subjectCode}</li>
            <li><strong>Student ID:</strong> ${studentId}</li>
            <li><strong>Email:</strong> ${email}</li>
            <li><strong>Day:</strong> ${day}</li>
            <li><strong>Time Schedule:</strong> ${timeSchedule}</li>
            <li><strong>Location:</strong> ${location}</li>
            <li><strong>Status:</strong> ${status}</li>
        </ul>
        <p>We will notify you once your reservation is approved.</p>
        <p>Thank you for using our reservation system.</p>
    </div>
</body>
</html>
