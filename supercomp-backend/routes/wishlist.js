import express from "express";
import Wishlist from "../models/Wishlist.js";

const router = express.Router();

// READ with FILTER – get wishlist for a user
router.get("/user/:userId", async (req, res) => {
  try {
    const items = await Wishlist.find({ userId: req.params.userId });
    res.json(items);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// WRITE – add a product to wishlist
router.post("/", async (req, res) => {
  try {
    const { userId, productId } = req.body;
    await Wishlist.create({ userId, productId });
    res.json({ message: "Added to wishlist" });
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

// DELETE – remove from wishlist
router.delete("/:id", async (req, res) => {
  try {
    await Wishlist.findByIdAndDelete(req.params.id);
    res.json({ message: "Removed from wishlist" });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

export default router;
