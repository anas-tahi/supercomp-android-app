import express from "express";
import bcrypt from "bcrypt";
import jwt from "jsonwebtoken";
import User from "../models/User.js";
import Comment from "../models/Comment.js";

const router = express.Router();

// REGISTER (NO EMAIL VERIFICATION)
router.post("/register", async (req, res) => {
  try {
    const { username, email, password } = req.body;

    // Check if email already exists
    const existing = await User.findOne({ email });
    if (existing) {
      return res.status(400).json({ error: "Email already used" });
    }

    // Hash password
    const hashedPassword = await bcrypt.hash(password, 10);

    // Create user
    await User.create({
      username,
      email,
      password: hashedPassword,
      verified: true
    });

    return res.json({ message: "User registered successfully" });

  } catch (err) {
    return res.status(400).json({ error: err.message });
  }
});

// LOGIN (NO EMAIL VERIFICATION REQUIRED)
router.post("/login", async (req, res) => {
  try {
    const { email, password } = req.body;

    // Check if user exists
    const user = await User.findOne({ email });
    if (!user) {
      return res.status(400).json({ error: "User not found" });
    }

    // Compare password
    const match = await bcrypt.compare(password, user.password);
    if (!match) {
      return res.status(400).json({ error: "Wrong password" });
    }

    // Generate JWT
    const token = jwt.sign(
      { id: user._id },
      process.env.JWT_SECRET,
      { expiresIn: "7d" }
    );

    return res.json({
      message: "Login successful",
      token,
      username: user.username
    });

  } catch (err) {
    return res.status(400).json({ error: err.message });
  }
});

// SAVE COMMENT
router.post("/comment", async (req, res) => {
  try {
    const { username, message } = req.body;

    await Comment.create({
      username,
      message,
      date: new Date()
    });

    return res.json({ message: "Comment saved" });

  } catch (err) {
    return res.status(400).json({ error: err.message });
  }
});

export default router;
  