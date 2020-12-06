# Robot-Delivery-System
It is a parcel delivery system for robots. Normal robots are only made to carry normal packages while special robots were made to deliver both normal and fragile items.

# System description
The robots gather mails from mailpool in floor 0 and deliver the mails to their proper floors. Normal robots can carry normal mails (2 mails maximum) and speical robots can carry normal and fragile mails (3 mails maximum with 1 fragile mail at most). The special robots use their special arms to wrap, deliver and unwrap their fragile mails.

# Code
The code uses Factory pattern to create types of robots according to if fragile items are present or not. Singleton were used for global information needed for mail delivery.

# How to alter the simulation

Iin "automail.properties", seed can be commented out for random mails. "Fragile" can be changed to decide if fragile items are present or not. "Caution" can be altered as well to ensure presense of cautious robots. "statistics" show a summary of the whole simulation. The other variables can be changed as well.
