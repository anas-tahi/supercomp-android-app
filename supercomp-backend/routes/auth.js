import express from "express";
import bcrypt from "bcrypt";
import jwt from "jsonwebtoken";
import User from "../models/User.js";

const router = express.Router();

// ── REGISTER ──────────────────────────────────────────────────────────────────
router.post("/register", async (req, res) => {
  try {
    const { username, email, password } = req.body;
    if (!username || !email || !password)
      return res.status(400).json({ error: "All fields are required." });

    const existing = await User.findOne({ email });
    if (existing)
      return res.status(400).json({ error: "Email already in use." });

    const hashedPassword = await bcrypt.hash(password, 10);
    await User.create({ username, email, password: hashedPassword });
    return res.json({ message: "User registered successfully." });
  } catch (err) {
    return res.status(500).json({ error: err.message });
  }
});

// ── LOGIN ─────────────────────────────────────────────────────────────────────
router.post("/login", async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email || !password)
      return res.status(400).json({ error: "Email and password are required." });

    const user = await User.findOne({ email });
    if (!user)
      return res.status(400).json({ error: "User not found." });

    const match = await bcrypt.compare(password, user.password);
    if (!match)
      return res.status(400).json({ error: "Wrong password." });

    const token = jwt.sign({ id: user._id }, process.env.JWT_SECRET, { expiresIn: "7d" });
    return res.json({
      message: "Login successful.",
      token,
      username: user.username,
      userId: user._id
    });
  } catch (err) {
    return res.status(500).json({ error: err.message });
  }
});

// ── GET PROFILE ───────────────────────────────────────────────────────────────
router.get("/profile/:userId", async (req, res) => {
  try {
    const user = await User.findById(req.params.userId).select("-password");
    if (!user) return res.status(404).json({ error: "User not found." });
    res.json(user);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ── UPDATE PROFILE ────────────────────────────────────────────────────────────
router.put("/profile/:userId", async (req, res) => {
  try {
    const { username, currentPassword, newPassword } = req.body;
    const user = await User.findById(req.params.userId);
    if (!user) return res.status(404).json({ error: "User not found." });

    // Update username if provided
    if (username && username.trim()) {
      user.username = username.trim();
    }

    // Update password if provided
    if (newPassword && currentPassword) {
      const match = await bcrypt.compare(currentPassword, user.password);
      if (!match)
        return res.status(400).json({ error: "Current password is incorrect." });
      user.password = await bcrypt.hash(newPassword, 10);
    }

    await user.save();
    res.json({ message: "Profile updated.", username: user.username });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

export default router;