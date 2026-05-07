import express from "express";
import Comment from "../models/Comment.js";

const router = express.Router();

// READ – all comments (newest first)
router.get("/", async (req, res) => {
  try {
    const comments = await Comment.find().sort({ createdAt: -1 });
    res.json(comments);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// WRITE – post a new comment
router.post("/", async (req, res) => {
  try {
    const { username, message } = req.body;
    if (!username || !message)
      return res.status(400).json({ error: "Username and message are required." });
    await Comment.create({ username, message });
    res.json({ message: "Comment saved." });
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

export default router;