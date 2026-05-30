import express from "express";
import bcrypt from "bcrypt";
import jwt from "jsonwebtoken";
import User from "../models/User.js";
import Wishlist from "../models/Wishlist.js";
import ShoppingList from "../models/ShoppingList.js";
import auth from "../middleware/auth.js";

const router = express.Router();

// Validate email
const isValidEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

// Helper function to validate password
const isValidPassword = (password) => {
  return password && password.length >= 8;
};

// REGISTER 
router.post("/register", async (req, res) => {
  try {
    const { username, email, password } = req.body;
    
    // Validation
    if (!username || !email || !password)
      return res.status(400).json({ error: "All fields are required." });
    
    if (username.trim().length < 3)
      return res.status(400).json({ error: "Username must be at least 3 characters." });
    
    if (!isValidEmail(email))
      return res.status(400).json({ error: "Invalid email format." });
    
    if (!isValidPassword(password))
      return res.status(400).json({ error: "Password must be at least 8 characters." });

    const existing = await User.findOne({ email });
    if (existing)
      return res.status(400).json({ error: "Email already in use." });

    const hashedPassword = await bcrypt.hash(password, 10);
    await User.create({ username: username.trim(), email: email.toLowerCase(), password: hashedPassword });
    return res.json({ message: "User registered successfully." });
  } catch (err) {
    return res.status(500).json({ error: err.message });
  }
});

// LOGIN
router.post("/login", async (req, res) => {
  try {
    const { email, password } = req.body;
    
    if (!email || !password)
      return res.status(400).json({ error: "Email and password are required." });

    const user = await User.findOne({ email: email.toLowerCase() });
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
      userId: user._id,
      email: user.email,
      profilePicture: user.profilePicture || "",
      phone: user.phone || "",
      city: user.city || ""
    });
  } catch (err) {
    return res.status(500).json({ error: err.message });
  }
});

// GET PROFILE 
router.get("/profile/:userId", auth, async (req, res) => {
  try {
    if (req.params.userId !== req.user.id) {
      return res.status(403).json({ error: "Forbidden: You can only access your own profile." });
    }
    
    const user = await User.findById(req.params.userId).select("-password");
    if (!user) return res.status(404).json({ error: "User not found." });
    res.json(user);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// UPDATE PROFILE
router.put("/profile/:userId", auth, async (req, res) => {
  try {
    if (req.params.userId !== req.user.id) {
      return res.status(403).json({ error: "Forbidden: You can only update your own profile." });
    }
    
    const { username, currentPassword, newPassword, profilePicture, phone, city } = req.body;
    const user = await User.findById(req.params.userId);
    if (!user) return res.status(404).json({ error: "User not found." });

    // Update username 
    if (username && username.trim()) {
      if (username.trim().length < 3) {
        return res.status(400).json({ error: "Username must be at least 3 characters." });
      }
      user.username = username.trim();
    }

    // Update profile picture
    if (profilePicture !== undefined) {
      user.profilePicture = profilePicture;
    }

    // Update phone
    if (phone !== undefined) {
      user.phone = phone;
    }

    // Update city 
    if (city !== undefined) {
      user.city = city;
    }

    // Update password 
    if (newPassword && currentPassword) {
      const match = await bcrypt.compare(currentPassword, user.password);
      if (!match)
        return res.status(400).json({ error: "Current password is incorrect." });
      
      if (!isValidPassword(newPassword))
        return res.status(400).json({ error: "New password must be at least 8 characters." });
      
      user.password = await bcrypt.hash(newPassword, 10);
    }

    await user.save();
    res.json({ 
      message: "Profile updated.", 
      username: user.username,
      profilePicture: user.profilePicture,
      phone: user.phone,
      city: user.city
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// DELETE ACCOUNT
router.delete("/account/:userId", auth, async (req, res) => {
  try {
    if (req.params.userId !== req.user.id) {
      return res.status(403).json({ error: "Forbidden: You can only delete your own account." });
    }
    
    const userId = req.params.userId;
    
    await Wishlist.deleteMany({ userId });
    await ShoppingList.deleteMany({ user: userId });
    await User.findByIdAndDelete(userId);
    
    res.json({ message: "Account and all associated data deleted successfully." });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

export default router;