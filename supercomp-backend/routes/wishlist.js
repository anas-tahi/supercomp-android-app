import express from "express";
import Wishlist from "../models/Wishlist.js";
import auth from "../middleware/auth.js";

const router = express.Router();

// GET wishlist for a user
router.get("/user/:userId", auth, async (req, res) => {
  try {
    if (req.params.userId !== req.user.id) {
      return res.status(403).json({ error: "Forbidden: You can only access your own wishlist." });
    }
    
    const items = await Wishlist.find({ userId: req.params.userId }).populate("productId");
    res.json(items);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// POST add to wishlist 
router.post("/", auth, async (req, res) => {
  try {
    const { userId, productId } = req.body;
        if (userId !== req.user.id) {
      return res.status(403).json({ error: "Forbidden: You can only add to your own wishlist." });
    }
    
    if (!userId || !productId) 
      return res.status(400).json({ error: "userId and productId required." });
    
    const existing = await Wishlist.findOne({ userId, productId });
    if (existing) 
      return res.status(400).json({ error: "Already in wishlist." });
    
    const newItem = await Wishlist.create({ userId, productId });
    res.json({ message: "Added to wishlist.", wishlistId: newItem._id });
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

// DELETE remove from wishlist 
router.delete("/:id", auth, async (req, res) => {
  try {
    const item = await Wishlist.findById(req.params.id);
    if (!item) {
      return res.status(404).json({ error: "Wishlist item not found." });
    }
        if (item.userId.toString() !== req.user.id) {
      return res.status(403).json({ error: "Forbidden: You can only remove from your own wishlist." });
    }
    
    await Wishlist.findByIdAndDelete(req.params.id);
    res.json({ message: "Removed from wishlist." });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

export default router;
