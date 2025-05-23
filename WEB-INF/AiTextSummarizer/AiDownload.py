from transformers import BartTokenizer, BartForConditionalGeneration
# Define the model name
model_name = "facebook/bart-large-cnn"
# Load the tokenizer and model
tokenizer = BartTokenizer.from_pretrained(model_name)
model = BartForConditionalGeneration.from_pretrained(model_name)
# Save the model and tokenizer locally
local_model_path = "./bart-large-cnn"
tokenizer.save_pretrained(local_model_path)
model.save_pretrained(local_model_path)
print(f"Model and tokenizer saved locally at: {local_model_path}")