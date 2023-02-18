dataset_info = dict(
    dataset_name='coco_odn',
    paper_info=dict(
        author='Lin, Tsung-Yi and Maire, Michael and '
        'Belongie, Serge and Hays, James and '
        'Perona, Pietro and Ramanan, Deva and '
        r'Doll{\'a}r, Piotr and Zitnick, C Lawrence',
        title='Microsoft coco: Common objects in context',
        container='European conference on computer vision',
        year='2014',
        homepage='http://cocodataset.org/',
    ),
    keypoint_info={
        0:
        dict(name='0201',id=0,color=[255, 128, 0],type='upper',swap='0216'),
        1:
        dict(name='0202',id=1,color=[255, 128, 0],type='upper',swap='0215'),
        2:
        dict(name='0203',id=2,color=[255, 128, 0],type='upper',swap='0214'),
        3:
        dict(name='0204',id=3,color=[255, 128, 0],type='upper',swap='0213'),
        4:
        dict(name='0205',id=4,color=[255, 128, 0],type='upper',swap='0212'),
        5:
        dict(name='0206',id=5,color=[255, 128, 0],type='upper',swap='0211'),
        6:
        dict(name='0207',id=6,color=[255, 128, 0],type='upper',swap='0210'),
        7:
        dict(name='0208',id=7,color=[255, 128, 0],type='upper',swap='0209'),
        8:
        dict(name='0209',id=8,color=[255, 128, 0],type='upper',swap='0208'),
        9:
        dict(name='0210',id=9,color=[255, 128, 0],type='upper',swap='0207'),
        10:
        dict(name='0211',id=10,color=[255, 128, 0],type='upper',swap='0206'),
        11:
        dict(name='0212',id=11,color=[255, 128, 0],type='upper',swap='0205'),
        12:
        dict(name='0213',id=12,color=[255, 128, 0],type='upper',swap='0204'),
        13:
        dict(name='0214',id=13,color=[255, 128, 0],type='upper',swap='0203'),
        14:
        dict(name='0215',id=14,color=[255, 128, 0],type='upper',swap='0202'),
        15:
        dict(name='0216',id=15,color=[255, 128, 0],type='upper',swap='0201'),
    },
    skeleton_info={
        0:
        dict(link=('0201', '0202'), id=0, color=[51, 153, 255]),
        1:
        dict(link=('0202', '0203'), id=1, color=[51, 153, 255]),
        2:
        dict(link=('0203', '0204'), id=2, color=[51, 153, 255]),
        3:
        dict(link=('0204', '0205'), id=3, color=[51, 153, 255]),
        4:
        dict(link=('0205', '0206'), id=4, color=[51, 153, 255]),
        5:
        dict(link=('0206', '0207'), id=5, color=[51, 153, 255]),
        6:
        dict(link=('0207', '0208'), id=6, color=[51, 153, 255]),
        7:
        dict(link=('0208', '0209'), id=7, color=[51, 153, 255]),
        8:
        dict(link=('0209', '0210'), id=8, color=[51, 153, 255]),
        9:
        dict(link=('0210', '0211'), id=9, color=[51, 153, 255]),
        10:
        dict(link=('0211', '0212'), id=10, color=[51, 153, 255]),
        11:
        dict(link=('0212', '0213'), id=11, color=[51, 153, 255]),
        12:
        dict(link=('0213', '0214'), id=12, color=[51, 153, 255]),
        13:
        dict(link=('0214', '0215'), id=13, color=[51, 153, 255]),
        14:
        dict(link=('0215', '0216'), id=14, color=[51, 153, 255])
    },
    joint_weights=[
        1., 1., 1., 1.2, 1., 1., 1., 1., 1., 1., 1.,1.,1.,1.,1.,1.
        # 1.5,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.,1.
    ],
    sigmas=[
        0.026, 0.025, 0.025, 0.035, 0.035, 0.079, 0.079, 0.072, 0.072, 0.082,
        0.082, 0.079, 0.072, 0.072, 0.082, 0.082
        #  0.107, 0.087, 0.087, 0.089, 0.089, 0.055, 0.022, 0.072,
        # 0.087, 0.087, 0.087, 0.087, 0.087, 0.087, 0.087, 0.087, 0.087, 0.087,
        # 0.087, 0.087, 0.087
    ]
    )