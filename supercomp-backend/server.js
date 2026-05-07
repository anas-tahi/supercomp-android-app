import express from "express";
import mongoose from "mongoose";
import cors from "cors";
import dotenv from "dotenv";
import authRoutes from "./routes/auth.js";
import productRoutes from "./routes/products.js";
import shoppingListRoutes from "./routes/shoppinglists.js";
import wishlistRoutes from "./routes/wishlist.js";
import commentRoutes from "./routes/comments.js";

dotenv.config();

const app = express();

app.use(cors());
app.use(express.json());

app.use("/auth", authRoutes);
app.use("/products", productRoutes);
app.use("/shoppinglists", shoppingListRoutes);
app.use("/wishlist", wishlistRoutes);
app.use("/comments", commentRoutes);

mongoose
  .connect(process.env.MONGO_URI)
  .then(() => console.log("MongoDB connected successfully"))
  .catch((err) => console.error("MongoDB connection error:", err));

app.get("/", (req, res) => {
  res.json({ message: "SuperComp backend v3 is running!" });
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, "0.0.0.0", () => {
  console.log(`Server running on port ${PORT}`);
});
