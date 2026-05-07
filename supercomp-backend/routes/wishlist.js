import express from "express";
import Wishlist from "../models/Wishlist.js";
import auth from "../middleware/auth.js";

const router = express.Router();

// GET wishlist for a user (protected)
router.get("/user/:userId", auth, async (req, res) => {
  try {
    const items = await Wishlist.find({ userId: req.params.userId }).populate("productId");
    res.json(items);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// POST add to wishlist (protected)
router.post("/", auth, async (req, res) => {
  try {
    const { userId, productId } = req.body;
    if (!userId || !productId) return res.status(400).json({ error: "userId and productId required." });
    const existing = await Wishlist.findOne({ userId, productId });
    if (existing) return res.status(400).json({ error: "Already in wishlist." });
    await Wishlist.create({ userId, productId });
    res.json({ message: "Added to wishlist." });
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

// DELETE remove from wishlist (protected)
router.delete("/:id", auth, async (req, res) => {
  try {
    await Wishlist.findByIdAndDelete(req.params.id);
    res.json({ message: "Removed from wishlist." });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

export default router;
