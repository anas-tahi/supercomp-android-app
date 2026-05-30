import mongoose from "mongoose";

const productSchema = new mongoose.Schema({
  name:        { type: String, required: true, trim: true },
  supermarket: {
    type: String,
    required: true,
    enum: ["Mercadona", "Lidl", "Carrefour", "Alcampo"]
  },
  price:       { type: Number, required: true },
  category:    { type: String, default: "General" },
  imageUrl:    { type: String, default: "" },
  source:      { type: String, enum: ["fake", "real", "legacy"], default: "legacy" }
}, { timestamps: true });

// Add indexes for better performance
productSchema.index({ name: 1, supermarket: 1 });
productSchema.index({ supermarket: 1 });
productSchema.index({ name: "text" }); // For text search

export default mongoose.model("Product", productSchema);
