import mongoose from "mongoose";

const commentSchema = new mongoose.Schema({
  username: { type: String, required: true },
  message:  { type: String, required: true, trim: true }
}, { timestamps: true });

export default mongoose.model("Comment", commentSchema);
