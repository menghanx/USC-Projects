#!/usr/bin/env python
# coding: utf-8

# In[20]:


import pandas as pd
import numpy as np
import nltk, re, json

import torch
import torch.nn as nn
import torch.nn.functional as F
import torch.optim as optim

from torch.utils.data import DataLoader
import torchvision.datasets as datasets
import torchvision.datasets as transforms

from sklearn.model_selection import train_test_split
from tqdm import tqdm


# In[38]:


# store train sentences 
train_file = 'data/train'
dev_file = 'data/dev'
test_file = 'data/test'
dummy_file ='data/dummy'

# read train/test file, each line as {s_idx, word, tag} tuple, store in a list
def readFile(file):
    f = open(file)
    lines = f.readlines()
    words = []
    for line in lines:
        if line.strip():
            words.append(line.strip().split(' '))
    return words

# DF: index - s_idx - word - tag
train_lines = readFile(train_file)
df = pd.DataFrame(train_lines, columns = ["s_idx", "word", "tag"])

# Randomly select some rare words to be <unk> words
unique_words = df["word"].value_counts().reset_index()
unique_words.columns = ["word", "freq"]

# Rare ward threshold
threshold = 3

# words with freq > threshold
vocab_words = unique_words[ unique_words['freq'] > threshold ]
# words with freq <= threshold
rare_words = unique_words[ unique_words['freq'] <= threshold ]

print("vocab words:", vocab_words.shape[0])
print("rare words:", rare_words.shape[0])

# Randomly select 3000 words from rare words to set as unknown words
# unk_count = len(rare_words)
# unk_words = rare_words.sample(unk_count)

# drop the selected rare words from vocab
# rare_words = rare_words.drop(unk_words.index)

# build new vocab = freq_words + rest_rare_words + <unk>
# vocab_words = vocab_words.append(rare_words, ignore_index=True)

# custom words unk, pad etc
custom_vocab = ['<unk>']
# custom_vocab = ['<unk>', '<pad>']

# main vocab list, to generate embedding
vocab_set = custom_vocab + vocab_words['word'].unique().tolist()
vocab_size = len(vocab_set)

# all the vocab
word_to_idx = {word:i for i, word in enumerate(vocab_set)}

# all the unique tags
unique_tags = set(df["tag"].unique())
tag_to_idx = {tag:i for i, tag in enumerate(unique_tags)}
idx_to_tag = {i:tag for i, tag in enumerate(unique_tags)}

# read files, group words by sentence, return list of sentences
def readData(file):
    f = open(file)
    lines = f.readlines()
    sentences = []
    sentence = []
    for line in lines:
        if not line.strip():
            sentences.append(sentence.copy())
            sentence.clear()
        else:
            sentence.append(line.strip().split(' '))
    # append the last sentence
    sentences.append(sentence.copy())
    return sentences

# word = [idx, word, tag]  train_data = list of sentences in term of list of words
train_data = readData(train_file)

dev_data = readData(dev_file)
# word = [idx, word]
test_data = readData(test_file)

# Dummy test data
dummy_file ='data/dummy'
dummy_data = readData(dummy_file)

# Preapare training data
def processData(tuples):
    training_data = []
    for t in tuples:
        training_data.append( ( [ word[1] if word[1] in word_to_idx else '<unk>' for word in t ], [ word[2] for word in t ] ) )
    return training_data

# Convert sequence into tensor
def prepare_sequence(seq, to_ix):
    idxs = [to_ix[w] for w in seq]
    return torch.tensor(idxs, dtype=torch.long)

def generateEvalFile1(model, input_data, file_name, word_to_idx):
    # Reset the file
    open(file_name, 'w').close()
    f = open(file_name, "a")
    
    # model eval mode
    model.eval()
    
    for t in input_data:
        sentence = [ word[1] if word[1] in word_to_idx else '<unk>' for word in t]
        with torch.no_grad():
            inputs = prepare_sequence(sentence, word_to_idx).to(device)
            tag_scores = model(inputs) 
            preds = [idx_to_tag[i] for i in torch.argmax(tag_scores, dim=1).tolist()]
            for word, pred in zip(t, preds):
                f.write(f'{word[0]} {word[1]} {word[2]} {pred}\n')
            f.write('\n')      
    f.close()
    
def processTestData(tuples):
    training_data = []
    for t in tuples:
        training_data.append( ( [ word[1] if word[1] in word_to_idx else '<unk>' for word in t ] ) )
    return training_data

def generateTestPred1(model, input_data, file_name):
    # Reset the file
    open(file_name, 'w').close()
    f = open(file_name, "a")
    
    # model eval mode
    model.eval()
    
    for t in input_data:

        sentence = [ word[1] if word[1] in word_to_idx else '<unk>' for word in t]
        with torch.no_grad():
            inputs = prepare_sequence(sentence, word_to_idx).to(device)
            tag_scores = model(inputs) 
            preds = [idx_to_tag[i] for i in torch.argmax(tag_scores, dim=1).tolist()]
            for word, pred in zip(t, preds):
                f.write(f'{word[0]} {word[1]} {pred}\n')
            f.write('\n')      
    f.close()


# In[22]:


training_data = processData(train_data)


# In[23]:


embedding_dim = 100
hidden_dim = 256
vocab_size = len(word_to_idx)
tagset_size = len(tag_to_idx)

lstm_layer = 1
lstm_dropout = 0.33
linear_out_dim = 128
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')


# In[338]:


# class LSTMTagger(nn.Module):
#     def __init__(self, embedding_dim, hidden_dim, vocab_size, tagset_size):
#         super(LSTMTagger, self).__init__()
#         self.hidden_dim = hidden_dim
        
#         self.word_embeddings = nn.Embedding(vocab_size, embedding_dim)
        
#         self.lstm = nn.LSTM(embedding_dim, hidden_dim)
#         self.hidden2tag = nn.Linear(hidden_dim, tagset_size)
    
#     # sentence [seq, batch, embed_dim]
#     def forward(self, sentence):
#         embeds = self.word_embeddings(sentence)
#         lstm_out, _ = self.lstm(embeds.view(len(sentence), 1, -1))
#         tag_space = self.hidden2tag(lstm_out.view(len(sentence), -1))
#         tag_scores = F.log_softmax(tag_space, dim=1)
#         return tag_scores


# In[24]:


class BLSTM(nn.Module):
    def __init__(self, embedding_dim, hidden_dim, vocab_size, tagset_size, linear_out_dim, 
                 lstm_layer, lstm_dropout):
        super(BLSTM, self).__init__()
        # word embedding
        self.word_embeddings = nn.Embedding(vocab_size, embedding_dim)
        self.bilstm = nn.LSTM(
            input_size=embedding_dim,
            hidden_size=hidden_dim,
            bidirectional=True)
        self.linear = nn.Linear(2*hidden_dim,linear_out_dim)
        self.fc = nn.Linear(linear_out_dim, tagset_size)
        self.dropout = nn.Dropout(lstm_dropout)
    
    def forward(self, sentence):
        # Embedding layer + LSTM input dropout
        embeds = self.word_embeddings(sentence)
        embeds = self.dropout(embeds)
        # BLSTM layer + LSTM output dropout
        lstm_out, _ = self.bilstm(embeds.view(len(sentence), 1, -1))
        lstm_out = self.dropout(lstm_out)
        # Linear layer + elu
        linear_out = F.elu(self.linear(lstm_out.view(len(sentence), -1)))
        # classifier
        tag_space = self.fc(linear_out)
        tag_scores = F.log_softmax(tag_space, dim=1)
        return tag_scores


# In[42]:


# Hyperparameter
lr = 0.05
epochs = 100
print_every = 10


# In[41]:


# model = LSTMTagger(embedding_dim, hidden_dim, vocab_size, tagset_size).to(device)
model = BLSTM(embedding_dim, hidden_dim, vocab_size, tagset_size, linear_out_dim, lstm_layer, 
              lstm_dropout).to(device)
loss_function = nn.NLLLoss()
# loss_func = nn.CrossEntropyLoss()
optimizer = optim.SGD(model.parameters(), lr=lr)


# In[19]:


dummy_file ='data/dummy'
dummy_data = readData(dummy_file)
dummy_data = processData(dummy_data)


# In[43]:


# Before training
# with torch.no_grad():
#     inputs = prepare_sequence(training_data[0][0], word_to_idx).to(device)
#     tag_scores = model(inputs)
#     print([idx_to_tag[i] for i in torch.argmax(tag_scores, dim=1).tolist()])


    
for epoch in range(epochs):
    for sentence, tags in tqdm(training_data, total=len(training_data)):
        model.zero_grad()
        
        sentence_in = prepare_sequence(sentence, word_to_idx).to(device)
        targets = prepare_sequence(tags, tag_to_idx).to(device)
        
        tag_scores = model(sentence_in)
        loss = loss_function(tag_scores, targets)
        loss.backward()
        optimizer.step()
    if (epoch+1)%print_every == 0:
        print(loss)
    
    
# After training
# with torch.no_grad():
#     inputs = prepare_sequence(training_data[0][0], word_to_idx).to(device)
#     tag_scores = model(inputs)
#     print([idx_to_tag[i] for i in torch.argmax(tag_scores, dim=1).tolist()])


# In[44]:


blstm1_name = "blstm1"
PATH = f"{blstm1_name}.pt"

# Save
torch.save(model, PATH)

# Load
model = torch.load(PATH)
model.eval()


# In[45]:


generateEvalFile1(model, dev_data, "dev1.out",word_to_idx)

generateTestPred1(model, test_data, "test1.out")


# # PART 2

# In[47]:


# Expect glove.6B.100d.txt to be unzipped 
embeddings_dict = {}
f = open('glove.6B.100d.txt', encoding='utf-8')
for line in f:
    values = line.split()
    word = values[0]
#     vector = torch.tensor(np.asarray(values[1:], dtype='float32'))
    vector = np.asarray(values[1:], dtype='float32')
    embeddings_dict[word] = vector
f.close()

train_lines = readFile(train_file)
df_train = pd.DataFrame(train_lines, columns = ["s_idx", "word", "tag"])

dev_lines = readFile(dev_file)
df_dev = pd.DataFrame(dev_lines, columns = ["s_idx", "word", "tag"])

test_lines = readFile(test_file)
df_test = pd.DataFrame(test_lines, columns = ["s_idx", "word"])

combo_df = df_train.append(df_dev).append(df_test)

# main vocab list, to generate embedding
vocab_set = set( vocab_words['word'].unique().tolist())
vocab_size = len(vocab_set)

# all the vocab
word_to_idx = {word:i for i, word in enumerate(vocab_set)}

# all the unique tags
unique_tags = set(df_train["tag"].unique())
tag_to_idx = {tag:i for i, tag in enumerate(unique_tags)}
idx_to_tag = {i:tag for i, tag in enumerate(unique_tags)}


# In[49]:


def findReplacement(word):
    if word.lower() in embeddings_dict:
        return word.lower()
    else:
        return "unk"
    

def processData2(tuples):
    training_data = []
    for t in tuples:
        training_data.append( ( [ word[1] if word[1] in embeddings_dict else findReplacement(word[1]) for word in t ], [ word[2] for word in t ] ) )
    return training_data

def prepare_glove_sequence(seq, to_ix):
    embeds = [embeddings_dict[word] for word in seq]
    return torch.tensor(embeds)

def generateEvalFile2(model, input_data, file_name, word_to_idx):
    # Reset the file
    open(file_name, 'w').close()
    f = open(file_name, "a")
    
    # model eval mode
    model.eval()
    
    for t in input_data:
        sentence = [ word[1] if word[1] in embeddings_dict else findReplacement(word[1]) for word in t ]
        with torch.no_grad():
            inputs = prepare_glove_sequence(sentence, word_to_idx).to(device)
            tag_scores = model(inputs) 
            preds = [idx_to_tag[i] for i in torch.argmax(tag_scores, dim=1).tolist()]
            for word, pred in zip(t, preds):
                f.write(f'{word[0]} {word[1]} {word[2]} {pred}\n')
            f.write('\n')      
    f.close()
    
def generateTestPred2(model, input_data, file_name):
    # Reset the file
    open(file_name, 'w').close()
    f = open(file_name, "a")
    
    # model eval mode
    model.eval()
    
    for t in input_data:
        sentence = [ word[1] if word[1] in embeddings_dict else findReplacement(word[1]) for word in t ]
        with torch.no_grad():
            inputs = prepare_glove_sequence(sentence, word_to_idx).to(device)
            tag_scores = model(inputs) 
            preds = [idx_to_tag[i] for i in torch.argmax(tag_scores, dim=1).tolist()]
            for word, pred in zip(t, preds):
                f.write(f'{word[0]} {word[1]} {pred}\n')
            f.write('\n')      
    f.close()


# In[50]:


class BLSTM2(nn.Module):
    def __init__(self, embedding_dim, hidden_dim, tagset_size, linear_out_dim, lstm_layer, lstm_dropout):
        super(BLSTM2, self).__init__()
        
        self.bilstm = nn.LSTM(
            input_size=embedding_dim,
            hidden_size=hidden_dim,
            bidirectional=True)
        self.linear = nn.Linear(2*hidden_dim,linear_out_dim)
        self.fc = nn.Linear(linear_out_dim, tagset_size)
        self.dropout = nn.Dropout(lstm_dropout)
    
    def forward(self, sentence):
        # Sentence input is tensor
        sentence = self.dropout(sentence)
        # BLSTM layer + LSTM output dropout
        lstm_out, _ = self.bilstm(sentence.view(len(sentence), 1, -1))
        lstm_out = self.dropout(lstm_out)
        # Linear layer + elu
        linear_out = F.elu(self.linear(lstm_out.view(len(sentence), -1)))
        # classifier
        tag_space = self.fc(linear_out)
        tag_scores = F.log_softmax(tag_space, dim=1)
        return tag_scores


# In[51]:


training_data = processData2(train_data)


# In[52]:


embedding_dim = 100
hidden_dim = 256
tagset_size = len(unique_tags)

lstm_layer = 1
lstm_dropout = 0.33
linear_out_dim = 128
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
# Hyperparameter
lr = 0.05
epochs = 50
print_every = 10

blstm2 = BLSTM2( embedding_dim, hidden_dim, tagset_size, linear_out_dim, lstm_layer, lstm_dropout ).to(device)

loss_function = nn.NLLLoss().to(device)
# loss_func = nn.CrossEntropyLoss()
optimizer = optim.SGD(blstm2.parameters(), lr=lr)


# In[53]:


for epoch in range(epochs):
    for sentence, tags in tqdm(training_data, total=len(training_data)):
        blstm2.zero_grad()
        
        sentence = prepare_glove_sequence(sentence, word_to_idx).to(device)
        tags = prepare_sequence(tags, tag_to_idx).to(device)
        
        tag_scores = blstm2(sentence)
        loss = loss_function(tag_scores, tags)
        loss.backward()
        optimizer.step()
    if (epoch+1)%print_every == 0:
        print(loss)


# In[ ]:


blstm1_name = "blstm2"
PATH = f"{blstm1_name}.pt"

# Save
torch.save(blstm2, PATH)

# Load
blstm2 = torch.load(PATH)
blstm2.eval()


# In[54]:


generateEvalFile2(blstm2, dev_data, "dev2.out", word_to_idx)

generateTestPred2(blstm2, test_data, "test2.out")

