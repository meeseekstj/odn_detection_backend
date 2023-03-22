import argparse
import time
import torch
import cv2
from numpy import random
from pathlib import Path
from models.experimental import attempt_load
from utils.datasets import LoadStreams, LoadImages
from utils.general import check_img_size, check_requirements, check_imshow, non_max_suppression, apply_classifier, \
    scale_coords, xyxy2xywh, strip_optimizer, set_logging, increment_path
from utils.plots import plot_one_box
from utils.torch_utils import select_device, load_classifier, time_synchronized, TracedModel


class DetectModel:
    def __init__(self,ModelWeightPath="./weights/best_tiny.pt",Device="0",
                ImageSize=640,Trace=True,Augment=True,ConfThres=0.25,
                IOUThres = 0.45,Classes = 3,AgnosticNMS = True):
        self.ModelWeightPath,self.AgnosticNMS = ModelWeightPath,AgnosticNMS
        self.ImageSize,self.IOUThres,self.Classes = ImageSize,IOUThres,Classes
        self.Trace,self.Augment,self.ConfThres = Trace,Augment,ConfThres
        self.Device = select_device(Device)
        self.Half = self.Device.type!="cpu"
        self.Model = attempt_load(self.ModelWeightPath, map_location=self.Device)  # load FP32 model
        self.Stride = int(self.Model.stride.max())
        self.ImageSize = check_img_size(ImageSize, s=self.Stride)
        if self.Trace:
            self.Model = TracedModel(self.Model, self.Device, ImageSize)

        if self.Half:
            self.Model.half()  # to FP16

    def __call__(self,ImageDataPath="./test/sample.jpg"):
        # 构建数据集
        dataset = LoadImages(ImageDataPath, img_size=self.ImageSize, stride=self.Stride)
        #
        names = self.Model.module.names if hasattr(self.Model, 'module') else self.Model.names
        colors = [[random.randint(0, 255) for _ in range(3)] for _ in names]
        # Run inference
        if self.Device.type != 'cpu':
            self.Model(torch.zeros(1, 3, self.ImageSize, self.ImageSize).to(self.Device).type_as(next(self.Model.parameters())))  # run once
        old_img_w = old_img_h = self.ImageSize
        old_img_b = 1
        #
        pointsList1,pointsList2,labels = [],[], []
        bboxes = []
        for path, img, im0s, vid_cap in dataset:
            img = torch.from_numpy(img).to(self.Device)
            img = img.half() if self.Half else img.float()  # uint8 to fp16/32
            img /= 255.0  # 0 - 255 to 0.0 - 1.0
            if img.ndimension() == 3:
                img = img.unsqueeze(0)

            # Warmup
            if self.Device.type != 'cpu' and (old_img_b != img.shape[0] or old_img_h != img.shape[2] or old_img_w != img.shape[3]):
                old_img_b = img.shape[0]
                old_img_h = img.shape[2]
                old_img_w = img.shape[3]
                for i in range(3):
                    self.Model(img, augment=self.Augment)[0]

            # Inference
            with torch.no_grad():   # Calculating gradients would cause a GPU memory leak
                pred = self.Model(img, augment=self.Augment)[0]

            # Apply NMS
            pred = non_max_suppression(pred, self.ConfThres, self.IOUThres, classes=None, agnostic=self.AgnosticNMS)
            # Process detections
            for i, det in enumerate(pred):  # detections per image
                p, s, im0, frame = path, '', im0s, getattr(dataset, 'frame', 0)

                p = Path(p)  # to Path
                gn = torch.tensor(im0.shape)[[1, 0, 1, 0]]  # normalization gain whwh
                if len(det):
                    # Rescale boxes from img_size to im0 size
                    det[:, :4] = scale_coords(img.shape[2:], det[:, :4], im0.shape).round()

                    # Print results
                    for c in det[:, -1].unique():
                        n = (det[:, -1] == c).sum()  # detections per class
                        s += f"{n} {names[int(c)]}{'s' * (n > 1)}, "  # add to string

                    # Write results
                    for *xyxy, conf, cls in reversed(det): # Add bbox to image
                        label = f'{names[int(cls)]} {conf:.2f}'
                        point1,point2,label = plot_one_box(xyxy, im0, label=label, color=colors[int(cls)], line_thickness=1)
                        labels.append(label)
                        bboxes.append([point1[0],point1[1],point2[0],point2[1]])
        return bboxes,labels